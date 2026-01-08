package com.qingmei.days.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingmei.days.utils.calculateDays
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCard(
    title: String,
    dateString: String,
    colorHex: Long,
    type: Int = 0,
    isTop: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val daysLeft = calculateDays(dateString)
    val themeColor = Color(colorHex)

    // 1. 背景：依然保持 15% 的通透果冻感
    val containerColor = themeColor.copy(alpha = 0.15f)

    // 2. 文字颜色智能校正：
    // 如果背景是浅糖果色，文字必须加深，否则看不清。
    // 这里我们做一个“颜色映射表”，把浅色自动映射为同色系的深色。
    val contentColor = when (colorHex) {
        // 黄色系 -> 变深橙
        0xFFFBC02D.toLong(), 0xFFFFF59D.toLong(), 0xFFFFEB3B.toLong() -> Color(0xFFF57F17)

        // 🌸 粉色系 (你觉得淡的那个) -> 变深玫瑰红
        0xFFF8BBD0.toLong(), 0xFFF48FB1.toLong() -> Color(0xFFC2185B)

        // 🌿 绿色系 -> 变深草绿
        0xFFA5D6A7.toLong() -> Color(0xFF2E7D32)

        // 🌊 青色系 -> 变深青
        0xFF80DEEA.toLong() -> Color(0xFF0097A7)

        // 🍆 紫色系 -> 变深紫
        0xFFCE93D8.toLong() -> Color(0xFF7B1FA2)

        // 其他颜色 (如珊瑚红) 本身够深，就用原色
        else -> themeColor
    }

    val labelText = when {
        type == 1 -> "已累计"
        daysLeft >= 0 -> "还有"
        else -> "已过期"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // 👇 3. 修改标题行：加上图钉图标
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.9f)
                    )

                    // 如果置顶了，显示一个小小的图钉
                    if (isTop) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = PushPinIcon,
                            contentDescription = "置顶",
                            tint = contentColor, // 颜色跟随主题色
                            modifier = Modifier.size(16.dp) // 小一点，精致
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(contentColor.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateString,
                        fontSize = 13.sp,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            // 右侧数字
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${abs(daysLeft)}",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        // 🌟 重点：数字现在会使用加深后的颜色，绝对显眼
                        color = contentColor,
                        style = androidx.compose.ui.text.TextStyle(
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "天",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = labelText,
                    fontSize = 12.sp,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

val PushPinIcon: ImageVector = ImageVector.Builder(
    name = "PushPin",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    // 这一行必须有，如果没有会自动报错
    path(fill = SolidColor(Color.Black)) {
        // 使用绝对坐标绘制 (MoveTo, LineTo, CurveTo)
        // 头部
        moveTo(16f, 9f)
        verticalLineTo(4f)
        horizontalLineTo(17f)
        verticalLineTo(2f)
        horizontalLineTo(7f)
        verticalLineTo(4f)
        horizontalLineTo(8f)
        verticalLineTo(9f)

        // 左侧弧度
        curveTo(8f, 10.5f, 5f, 12f, 5f, 12f)
        verticalLineTo(14f)
        horizontalLineTo(11f)

        // 针尖
        verticalLineTo(21f)
        lineTo(12f, 22f)
        lineTo(13f, 21f)
        verticalLineTo(14f)
        horizontalLineTo(19f)
        verticalLineTo(12f)

        // 右侧弧度
        curveTo(19f, 12f, 16f, 10.5f, 16f, 9f)
        close()
    }
}.build()