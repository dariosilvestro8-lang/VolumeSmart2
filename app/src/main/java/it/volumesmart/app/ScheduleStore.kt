package it.volumesmart.app

import android.content.Context
import org.json.JSONArray

object ScheduleStore {

    private const val PREFS = "volume_smart_prefs"
    private const val KEY_SCHEDULES = "schedules"
    private const val KEY_OFF_VOLUME = "off_volume"

    fun getSchedules(context: Context): MutableList<Schedule> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SCHEDULES, "[]") ?: "[]"

        return try {
            val array = JSONArray(json)
            MutableList(array.length()) { index ->
                Schedule.fromJson(array.getJSONObject(index))
            }
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveSchedules(context: Context, schedules: List<Schedule>) {
        val array = JSONArray()
        schedules.forEach { array.put(it.toJson()) }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SCHEDULES, array.toString())
            .apply()
    }

    fun getOffVolume(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_OFF_VOLUME, 30)
    }

    fun saveOffVolume(context: Context, volume: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_OFF_VOLUME, volume.coerceIn(0, 100))
            .apply()
    }
}
