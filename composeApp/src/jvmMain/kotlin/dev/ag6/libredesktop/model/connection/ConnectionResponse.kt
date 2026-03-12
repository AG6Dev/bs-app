package dev.ag6.libredesktop.model.connection

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionData(
    val id: String,
    val patientId: String,
    val country: String,
    val status: Int,
    val firstName: String,
    val lastName: String,
    val targetLow: Int,
    val targetHigh: Int,
    val uom: String,
//    val sensor: Sensor,
//    val alarmRules: AlarmRules,
    val glucoseMeasurement: GlucoseItem,
    val glucoseItem: GlucoseItem,
//    val glucoseAlarm
//    val patientDevice: PatientDevice,
    val created: Long
)

@Serializable
data class GlucoseItem(
    @SerialName("FactoryTimestamp") val factoryTimestamp: String,
    @SerialName("Timestamp") val timestamp: String,
    val type: Int,
    @SerialName("ValueInMgPerDl") val valueInMgPerDl: Int,
    @SerialName("TrendArrow") val trendArrow: Int? = null,
    @SerialName("TrendMessage") val trendMessage: String? = null,
    @SerialName("MeasurementColor") val measurementColor: Int,
    @SerialName("GlucoseUnits") val glucoseUnits: Int,
    @SerialName("Value") val value: Float,
    val isHigh: Boolean,
    val isLow: Boolean
)
