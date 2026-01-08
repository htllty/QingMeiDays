package com.qingmei.days.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.google.gson.Gson
import com.qingmei.days.MainActivity
import com.qingmei.days.model.LifeEvent
import com.qingmei.days.utils.DataManager
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class MyWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

//        不准在 provideGlance 外读数据
//
//        必须用 currentState<Preferences>()
//
//        key 名字和 DataManager 完全一致

        provideContent {
            val prefs = currentState<Preferences>()

            val widgetVersion =
                prefs[DataManager.WIDGET_VERSION_KEY] ?: 0

            val json =
                prefs[DataManager.WIDGET_EVENT_JSON]

            val event = json?.takeIf { it.isNotEmpty() }?.let {
                Gson().fromJson(it, LifeEvent::class.java)
            }

            WidgetContent(event, widgetVersion)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun WidgetContent(
        event: LifeEvent?,
        widgetVersion: Int
    ) {
        // 默认色兜底
        val baseColor = if (event != null) Color(event.color) else Color(0xFF00BCD4)
        val themeColor = baseColor.copy(alpha = 0.6f)
        val baseTextColor = Color.White

        // 点击跳转参数
        val idKey = ActionParameters.Key<String>("target_id")
        val clickAction = if (event != null) {
            actionStartActivity<MainActivity>(
                actionParametersOf(idKey to event.id)
            )
        } else {
            actionStartActivity<MainActivity>()
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .clickable(clickAction),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(themeColor)
                    .cornerRadius(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // ⭐ 隐形 Text：强制依赖 widgetVersion
                    // 只要 version 变了，整个 WidgetContent 就会重绘
                    Text(
                        text = "$widgetVersion",
                        modifier = GlanceModifier.size(0.dp)
                    )

                    if (event != null) {
                        val targetDate = try {
                            LocalDate.parse(event.date)
                        } catch (e: Exception) {
                            LocalDate.now()
                        }

                        val days = ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            targetDate
                        )

                        Text(
                            text = event.title,
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = ColorProvider(baseTextColor.copy(alpha = 0.9f)),
                                fontWeight = androidx.glance.text.FontWeight.Medium
                            )
                        )

                        Spacer(GlanceModifier.height(4.dp))

                        Text(
                            text = "${abs(days)}",
                            style = TextStyle(
                                fontSize = 48.sp,
                                fontWeight = androidx.glance.text.FontWeight.Bold,
                                color = ColorProvider(baseTextColor)
                            )
                        )

                        Text(
                            text = if (days >= 0) "还有几天" else "已经过去",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = ColorProvider(baseTextColor.copy(alpha = 0.7f))
                            )
                        )
                    } else {
                        // 空状态
                        Text(
                            text = "点击添加日子",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = ColorProvider(baseTextColor.copy(alpha = 0.8f))
                            )
                        )
                    }
                }
            }
        }
    }
}