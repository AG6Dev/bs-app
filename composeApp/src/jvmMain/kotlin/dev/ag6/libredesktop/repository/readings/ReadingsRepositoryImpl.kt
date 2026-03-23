package dev.ag6.libredesktop.repository.readings

import com.russhwolf.settings.Settings
import dev.ag6.libredesktop.api.LibreApiCallResult
import dev.ag6.libredesktop.api.buildLibreApiUrl
import dev.ag6.libredesktop.api.executeLibreApiRequest
import dev.ag6.libredesktop.api.getLibreApiRegion
import dev.ag6.libredesktop.model.connection.ConnectionData
import dev.ag6.libredesktop.model.connection.GraphConnectionData
import dev.ag6.libredesktop.model.reading.GlucoseReading
import dev.ag6.libredesktop.model.reading.mapToGlucoseReading
import dev.ag6.libredesktop.repository.auth.AuthRepository
import io.ktor.client.*
import io.ktor.client.request.*
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
        private const val CONNECTIONS_PATH = "llu/connections"
        private const val PATIENT_ID_KEY = "patient_id"
    }

    override suspend fun getCurrentReading(): GlucoseReading? {
        val response = makeGetRequest(
            CONNECTIONS_PATH,
            ListSerializer(ConnectionData.serializer())
        ) ?: return null

        return when (response) {
            is LibreApiCallResult.Success -> {
                settings.putString(PATIENT_ID_KEY, response.data.first().patientId)
                response.data.first().glucoseItem.mapToGlucoseReading()
            }

            is LibreApiCallResult.Failure -> null
        }
    }

    override suspend fun getGraphReadings(): List<GlucoseReading> {
        val pid = getPatientId() ?: return emptyList()
        val response = makeGetRequest(
            "$CONNECTIONS_PATH/$pid/graph",
            GraphConnectionData.serializer()
        ) ?: return emptyList()

        return when (response) {
            is LibreApiCallResult.Success -> response.data.graphData.map { it.mapToGlucoseReading() }
            is LibreApiCallResult.Failure -> emptyList()
        }
    }

    private suspend fun <T> makeGetRequest(
        path: String,
        dataSerializer: KSerializer<T>
    ): LibreApiCallResult<T>? {
        val userId: String = authRepository.getUserId() ?: return null
        val token: String = authRepository.getAuthToken() ?: return null

        val userHash = MessageDigest.getInstance("SHA-256")
            .digest(userId.toByteArray())
            .joinToString("") { "%02x".format(it) }

        return executeLibreApiRequest(
            settings = settings,
            json = json,
            initialRegion = settings.getLibreApiRegion(),
            successSerializer = dataSerializer
        ) { region ->
            httpClient.get(buildLibreApiUrl(region, path)) {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                headers {
                    append("Accept-Encoding", "gzip")
                    append("product", "llu.android")
                    append("version", "4.16.0")
                    append("Account-Id", userHash)
                }
            }
        }
    }

    override suspend fun getPatientId(): String? {
        return settings.getStringOrNull(PATIENT_ID_KEY)
    }
}
