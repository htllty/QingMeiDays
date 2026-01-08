package com.qingmei.days.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageSaver {

    /**
     * 将 Picture (录制的画面) 转换为 Bitmap
     */
    fun createBitmapFromPicture(picture: android.graphics.Picture): Bitmap {
        val bitmap = Bitmap.createBitmap(
            picture.width.coerceAtLeast(1),
            picture.height.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        // 🌟 必须刷一层白底，防止截出来是黑色的
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawPicture(picture)
        return bitmap
    }

    /**
     * 保存到系统相册
     */
    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                // 建议加上时间戳，防止重名导致保存失败
                val name = "${fileName}_${System.currentTimeMillis()}"
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "QingMeiDays")
                    put(MediaStore.MediaColumns.IS_PENDING, 1) // 🌟 标记：正在写入
                }
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                try {
                    resolver.openOutputStream(imageUri)?.use { output ->
                        // 压缩并写入数据
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0) // 🌟 标记：写入完成，此时相册才会显示
                        resolver.update(imageUri, contentValues, null, null)
                    }
                    true
                } catch (e: Exception) {
                    resolver.delete(imageUri, null, null)
                    false
                }
            } else false
        }
    }

}

// ✨ 这里就是你缺少的那个 showToast 函数
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}