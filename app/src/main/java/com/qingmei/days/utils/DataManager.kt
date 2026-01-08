package com.qingmei.days.utils

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qingmei.days.components.MyWidget
import com.qingmei.days.model.LifeEvent

// ❌ 删掉下面这行，Widget 不需要读这个全局 DataStore
// val Context.dataStore by preferencesDataStore("qingmei_days_widget")

object DataManager {

    private const val PREFS_NAME = "qingmei_days_prefs"
    private const val KEY_EVENTS = "saved_events"

    // ⭐ 必须确保 key 的名字和 MyWidget 里写的一模一样
    val WIDGET_VERSION_KEY = intPreferencesKey("widget_version")
    val WIDGET_EVENT_JSON = stringPreferencesKey("widget_event_json")

    private val gson = Gson()

    /**
     * ⭐ 修正后的逻辑：
     * 1. App 数据存 SP (不变)
     * 2. Widget 数据直接注入到 Glance 的 State 里 (这才是 currentState 能读到的地方)
     */
    suspend fun saveAndSyncWidget(context: Context, events: List<LifeEvent>) {
        // 1. 存入 SharedPreferences (App 内部数据)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = gson.toJson(events)
        prefs.edit().putString(KEY_EVENTS, jsonString).apply()

        // 2. 计算 Widget 要显示的数据
        val displayEvent = events.find { it.isTop } ?: events.firstOrNull()
        val widgetJson = if (displayEvent != null) gson.toJson(displayEvent) else ""

        // 3. ⭐ 核心修改：遍历所有 Widget，把数据塞进它们自己的 State 里
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(MyWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[WIDGET_EVENT_JSON] = widgetJson
                val oldVersion = prefs[WIDGET_VERSION_KEY] ?: 0
                prefs[WIDGET_VERSION_KEY] = oldVersion + 1
            }
        }

        MyWidget().updateAll(context)
    }

    // App 启动加载 (保持不变)
    fun loadEvents(context: Context): MutableList<LifeEvent> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_EVENTS, null)
        return if (jsonString != null) {
            try {
                val type = object : TypeToken<List<LifeEvent>>() {}.type
                gson.fromJson(jsonString, type)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
    }
}