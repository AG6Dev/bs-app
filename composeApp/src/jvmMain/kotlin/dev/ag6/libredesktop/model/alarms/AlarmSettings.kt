package dev.ag6.libredesktop.model.alarms

import kotlinx.serialization.Serializable

@Serializable
data class AlarmSettings(
    val alarmsEnabled: Boolean = false,
    val alarmInterval: Int = 5,
    val soundEnabled: Boolean = false,
    val customSoundPath: String? = null,
    val notificationsEnabled: Boolean = false
) {
    companion object {
        val ALARM_INTERVALS = listOf(1, 5, 15, 30)
    }
}