package dev.ag6.libredesktop.repository.readings

import com.russhwolf.settings.Settings
import dev.ag6.libredesktop.api.LibreApiResponse
import dev.ag6.libredesktop.api.decodeLibreApiResponse
import dev.ag6.libredesktop.model.connection.ConnectionData
import dev.ag6.libredesktop.model.connection.GraphConnectionData
import dev.ag6.libredesktop.model.reading.GlucoseReading
import dev.ag6.libredesktop.model.reading.mapToGlucoseReading
import dev.ag6.libredesktop.repository.auth.AuthRepository
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.security.MessageDigest

class ReadingsRepositoryImpl(
    val httpClient: HttpClient,
    val authRepository: AuthRepository,
    val settings: Settings,
    val json: Json
) : ReadingsRepository {
    companion object {
        private const val CONNECTIONS_URL = "https://api.libreview.io/llu/connections"
        private const val PATIENT_ID_KEY = "patient_id"
    }

    override suspend fun getCurrentReading(): GlucoseReading? {
        val response = makeGetRequest(CONNECTIONS_URL, ListSerializer(ConnectionData.serializer())) ?: return null

        return when (response) {
            is LibreApiResponse.Success -> {
                settings.putString(PATIENT_ID_KEY, response.data.first().patientId)
                response.data.first().glucoseItem.mapToGlucoseReading()
            }

            is LibreApiResponse.Error -> null
            is LibreApiResponse.Redirect -> null
        }
    }

    override suspend fun getGraphReadings(): List<GlucoseReading> {
        val pid = getPatientId() ?: return emptyList()
        val response = makeGetRequest(
            "$CONNECTIONS_URL/$pid/graph",
            GraphConnectionData.serializer()
        ) ?: return emptyList()

        return when (response) {
            is LibreApiResponse.Success -> response.data.graphData.map { it.mapToGlucoseReading() }
            is LibreApiResponse.Error -> emptyList()
            is LibreApiResponse.Redirect -> emptyList()
        }
    }

    private suspend fun <T> makeGetRequest(
        url: String,
        dataSerializer: KSerializer<T>
    ): LibreApiResponse<T>? {
        val userId: String = authRepository.getUserId() ?: return null
        val token: String = authRepository.getAuthToken() ?: return null

        val userHash = MessageDigest.getInstance("SHA-256")
            .digest(userId.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val response = httpClient.get(url) {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            headers {
                append("Accept-Encoding", "gzip")
                append("product", "llu.android")
                append("version", "4.16.0")
                append("Account-Id", userHash)
            }
        }

        return if (response.status.isSuccess()) {
            decodeLibreApiResponse(response.bodyAsText(), dataSerializer, json)
        } else {
            null
        }
    }

    override suspend fun getPatientId(): String? {
        return settings.getStringOrNull(PATIENT_ID_KEY)
    }
}
