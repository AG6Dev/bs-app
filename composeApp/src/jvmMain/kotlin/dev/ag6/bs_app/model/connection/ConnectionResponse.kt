package dev.ag6.bs_app.model.connection

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

object ConnectionResponseSerializer : JsonContentPolymorphicSerializer<ConnectionResponse>(ConnectionResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ConnectionResponse> {
        return if (element.jsonObject.containsKey("data")) ConnectionResponse.Success.serializer() else ConnectionResponse.Error.serializer()
    }
}

@Serializable(with = ConnectionResponseSerializer::class)
sealed class ConnectionResponse {
    @Serializable
    data class Success(val status: Int, val data: List<ConnectionData>) : ConnectionResponse()

    @Serializable
    data class Error(val status: Int, val message: Message) : ConnectionResponse() {
        @Serializable
        data class Message(val message: String)
    }
}

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
