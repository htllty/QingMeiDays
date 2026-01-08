package com.qingmei.days.utils

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.qingmei.days.components.MyWidget

object WidgetRefresher {

    suspend fun refresh(context: Context) {
        try {
            // ⭐ 这一行代码 = 你的方案 A 全家桶
            // 它会自动查找所有已添加的 MyWidget 实例，并触发 provideGlance 重绘
            MyWidget().updateAll(context)

            Log.d("WidgetRefresh", "Glance 刷新指令已发送")
        } catch (e: Exception) {
            Log.e("WidgetRefresh", "Glance 刷新失败", e)
        }
    }
}