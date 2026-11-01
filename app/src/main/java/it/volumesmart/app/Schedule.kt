
package it.volumesmart.app

import org.json.JSONObject

data class Schedule(
    val id: Int,
    val name: String,
    val start: String,
    val end: String,
    val volume: Int,
    val enabled: Boolean = true
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("start", start)
            put("end", end)
            put("volume", volume)
            put("enabled", enabled)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): Schedule {
            return Schedule(
                id = json.optInt("id"),
                name = json.optString("name", "Nuova fascia"),
                start = json.optString("start", "08:00"),
                end = json.optString("end", "09:00"),
                volume = json.optInt("volume", 50)
                    .coerceIn(0, 100),
                enabled = json.optBoolean("enabled", true)
            )
        }
    }
}
