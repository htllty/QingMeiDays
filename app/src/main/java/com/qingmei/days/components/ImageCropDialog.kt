package com.qingmei.days.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Picture
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qingmei.days.utils.ImageSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

@Composable
fun ImageCropDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageDisplaySize by remember { mutableStateOf(Size.Zero) }
    var viewSize by remember { mutableStateOf(Size.Zero) } // 屏幕/容器尺寸

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // 加载图片
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(imageUri)?.use {
                    originalBitmap = BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onGloballyPositioned { viewSize = it.size.toSize() }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (imageDisplaySize != Size.Zero && viewSize != Size.Zero) {
                            // 🌟 核心修改 1：裁剪框边长 = 屏幕宽度 (正方形)
                            val cropSide = viewSize.width

                            // 1. 计算最小缩放 (图片必须比正方形大)
                            val minScaleX = cropSide / imageDisplaySize.width
                            val minScaleY = cropSide / imageDisplaySize.height
                            val dynamicMinScale = max(minScaleX, minScaleY)

                            val newScale = (scale * zoom).coerceIn(dynamicMinScale, 5f)

                            // 2. 计算位移边界
                            val currentWidth = imageDisplaySize.width * newScale
                            val currentHeight = imageDisplaySize.height * newScale

                            // 允许拖动的最大距离 = (当前图尺寸 - 正方形尺寸) / 2
                            val maxOffsetX = max(0f, (currentWidth - cropSide) / 2f)
                            val maxOffsetY = max(0f, (currentHeight - cropSide) / 2f)

                            val tempOffset = offset + pan
                            val clampedX = tempOffset.x.coerceIn(-maxOffsetX, maxOffsetX)
                            val clampedY = tempOffset.y.coerceIn(-maxOffsetY, maxOffsetY)

                            scale = newScale
                            offset = Offset(clampedX, clampedY)
                        }
                    }
                }
        ) {
            if (originalBitmap != null) {
                // 图层 1：图片
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = originalBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit, // 确保初始加载时能完整看到图片宽度
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                            .onGloballyPositioned { coordinates ->
                                if (imageDisplaySize == Size.Zero) {
                                    imageDisplaySize = coordinates.size.toSize()
                                    // 初始加载检查：如果高度小于宽度（横图），必须放大填满正方形
                                    if (imageDisplaySize.height < imageDisplaySize.width) {
                                        scale = imageDisplaySize.width / imageDisplaySize.height
                                    }
                                }
                            }
                    )
                }

                // 图层 2：遮罩 (正方形)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // 🌟 核心修改 2：定义正方形区域
                    val cropSide = canvasWidth // 边长 = 屏幕宽度
                    val cropRect = Rect(
                        left = 0f,
                        top = (canvasHeight - cropSide) / 2,
                        right = cropSide,
                        bottom = (canvasHeight + cropSide) / 2
                    )

                    drawPath(
                        path = Path().apply {
                            addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                            addRect(cropRect)
                            fillType = PathFillType.EvenOdd
                        },
                        color = Color.Black.copy(alpha = 0.7f)
                    )

                    // 白色边框
                    drawRect(color = Color.White, topLeft = cropRect.topLeft, size = cropRect.size, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))

                    // 九宫格辅助线 (变成正方形网格)
                    val oneThird = cropSide / 3
                    drawLine(Color.White.copy(0.3f), Offset(0f, cropRect.top + oneThird), Offset(canvasWidth, cropRect.top + oneThird))
                    drawLine(Color.White.copy(0.3f), Offset(0f, cropRect.top + oneThird * 2), Offset(canvasWidth, cropRect.top + oneThird * 2))
                    drawLine(Color.White.copy(0.3f), Offset(oneThird, cropRect.top), Offset(oneThird, cropRect.bottom))
                    drawLine(Color.White.copy(0.3f), Offset(oneThird * 2, cropRect.top), Offset(oneThird * 2, cropRect.bottom))
                }

                Text("拖动和缩放 (1:1)", color = Color.White, modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp))

                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Color.White.copy(0.2f), CircleShape)) { Icon(Icons.Default.Close, "取消", tint = Color.White) }
                    IconButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.Default) {
                                if (imageDisplaySize != Size.Zero) {
                                    val result = captureCrop(
                                        originalBitmap = originalBitmap!!,
                                        imageDisplaySize = imageDisplaySize,
                                        // 🌟 核心修改 3：输出宽高都是屏幕宽度 (正方形)
                                        cropSize = viewSize.width.toInt(),
                                        userScale = scale,
                                        userOffset = offset
                                    )
                                    withContext(Dispatchers.Main) { onConfirm(result) }
                                }
                            }
                        },
                        modifier = Modifier.size(64.dp).background(Color(0xFF00BCD4), CircleShape)
                    ) { Icon(Icons.Default.Check, "确认", tint = Color.White, modifier = Modifier.size(32.dp)) }
                }
            } else {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

private fun captureCrop(
    originalBitmap: Bitmap,
    imageDisplaySize: Size,
    cropSize: Int, // 现在只需要一个边长
    userScale: Float,
    userOffset: Offset
): Bitmap {
    val picture = Picture()
    // 画布是正方形
    val canvas = picture.beginRecording(cropSize, cropSize)

    // 移到中心
    canvas.translate(cropSize / 2f, cropSize / 2f)
    canvas.translate(userOffset.x, userOffset.y)
    canvas.scale(userScale, userScale)

    val baseScale = imageDisplaySize.width / originalBitmap.width
    canvas.scale(baseScale, baseScale)

    canvas.translate(-originalBitmap.width / 2f, -originalBitmap.height / 2f)
    canvas.drawBitmap(originalBitmap, 0f, 0f, null)

    picture.endRecording()
    return ImageSaver.createBitmapFromPicture(picture)
}