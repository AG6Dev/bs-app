package dev.ag6.libredesktop.api

import com.russhwolf.settings.Settings
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.*

sealed interface LibreApiEnvelope<out T> {
    data class Success<T>(val status: Int, val data: T) : LibreApiEnvelope<T>
    data class Redirect(val status: Int, val region: String) : LibreApiEnvelope<Nothing>
    data class Error(val status: Int?, val message: String?) : LibreApiEnvelope<Nothing>
}

sealed interface LibreApiCallResult<out T> {
    data class Success<T>(val data: T) : LibreApiCallResult<T>
    data class Failure(val status: Int? = null, val message: String? = null) : LibreApiCallResult<Nothing>
}

internal suspend fun <T> executeLibreApiRequest(
    settings: Settings,
    json: Json,
    initialRegion: String?,
    successSerializer: KSerializer<T>,
    request: suspend (String?) -> HttpResponse
): LibreApiCallResult<T> {
    val firstAttempt = executeLibreApiRequestOnce(initialRegion, successSerializer, request, json)
    if (firstAttempt !is LibreApiEnvelope.Redirect) {
        if (firstAttempt is LibreApiEnvelope.Success && !initialRegion.isNullOrBlank()) {
            settings.setLibreApiRegion(initialRegion)
        }
        return firstAttempt.toCallResult()
    }

    settings.setLibreApiRegion(firstAttempt.region)
    return when (val redirectedAttempt =
        executeLibreApiRequestOnce(firstAttempt.region, successSerializer, request, json)) {
        is LibreApiEnvelope.Success -> LibreApiCallResult.Success(redirectedAttempt.data)
        is LibreApiEnvelope.Error -> redirectedAttempt.toCallResult()
        is LibreApiEnvelope.Redirect -> LibreApiCallResult.Failure(
            status = redirectedAttempt.status,
            message = "LibreView redirected the request more than once."
        )
    }
}

internal fun <T> decodeLibreApiEnvelope(
    responseText: String,
    successSerializer: KSerializer<T>,
    json: Json
): LibreApiEnvelope<T> {
    val payload = json.parseToJsonElement(responseText).jsonObject
    val status = payload["status"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
    val data = payload["data"]
    val message = payload.messageText()

    return when {
        data.isRedirectPayload() -> {
            val region = data!!.jsonObject["region"]?.jsonPrimitive?.contentOrNull
                ?: return LibreApiEnvelope.Error(
                    status = status,
                    message = "LibreView redirect did not include a region."
                )
            LibreApiEnvelope.Redirect(status = status, region = region)
        }

        data != null -> LibreApiEnvelope.Success(
            status = status,
            data = json.decodeFromJsonElement(successSerializer, data)
        )

        message != null -> LibreApiEnvelope.Error(status = status, message = message)
        else -> LibreApiEnvelope.Error(status = status, message = "Unknown LibreView response shape: ${payload.keys}")
    }
}

private suspend fun <T> executeLibreApiRequestOnce(
    region: String?,
    successSerializer: KSerializer<T>,
    request: suspend (String?) -> HttpResponse,
    json: Json
): LibreApiEnvelope<T> {
    val response = request(region)
    val body = response.bodyAsText()

    return try {
        val envelope = decodeLibreApiEnvelope(body, successSerializer, json)
        if (!response.status.isSuccess() && envelope is LibreApiEnvelope.Success) {
            LibreApiEnvelope.Error(
                status = response.status.value,
                message = "LibreView request failed with HTTP ${response.status.value}."
            )
        } else {
            envelope
        }
    } catch (_: Exception) {
        LibreApiEnvelope.Error(
            status = response.status.value,
            message = "LibreView request failed with HTTP ${response.status.value}."
        )
    }
}

private fun <T> LibreApiEnvelope<T>.toCallResult(): LibreApiCallResult<T> = when (this) {
    is LibreApiEnvelope.Success -> LibreApiCallResult.Success(data)
    is LibreApiEnvelope.Error -> LibreApiCallResult.Failure(status = status, message = message)
    is LibreApiEnvelope.Redirect -> LibreApiCallResult.Failure(
        status = status,
        message = "LibreView redirected the request unexpectedly."
    )
}

private fun JsonElement?.isRedirectPayload(): Boolean {
    val dataObject = this as? JsonObject ?: return false
    return dataObject["redirect"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() == true
}

private fun JsonObject.messageText(): String? {
    return parseMessageField(this["message"]) ?: parseMessageField(this["error"])
}

private fun parseMessageField(field: JsonElement?): String? {
    val fieldObject = field as? JsonObject ?: return null
    return fieldObject["message"]?.jsonPrimitive?.contentOrNull
}
