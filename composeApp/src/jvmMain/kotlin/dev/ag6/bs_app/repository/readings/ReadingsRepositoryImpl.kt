package dev.ag6.bs_app.repository.readings

import com.russhwolf.settings.Settings
import dev.ag6.bs_app.model.connection.ConnectionResponse
import dev.ag6.bs_app.model.connection.GraphResponse
import dev.ag6.bs_app.model.reading.GlucoseReading
import dev.ag6.bs_app.model.reading.mapToGlucoseReading
import dev.ag6.bs_app.repository.auth.AuthRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.security.MessageDigest
import kotlin.collections.emptyList

class ReadingsRepositoryImpl(
    val httpClient: HttpClient,
    val authRepository: AuthRepository,
    val settings: Settings
) : ReadingsRepository {
    companion object {
        private const val CONNECTIONS_URL = "https://api.libreview.io/llu/connections"
        private const val PATIENT_ID_KEY = "patient_id"
    }

    override suspend fun getCurrentReading(): GlucoseReading? {
        val userId = authRepository.getUserId() ?: return null
        val token = authRepository.getAuthToken() ?: return null

        val userHash = MessageDigest.getInstance("SHA-256")
            .digest(userId.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val response = httpClient.get(CONNECTIONS_URL) {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            headers {
                append("product", "llu.android")
                append("version", "4.16.0")
                append("Account-Id", userHash)
            }
        }
        val currentReading = when (val connection = response.body<ConnectionResponse>()) {
            is ConnectionResponse.Success -> {
                settings.putString(PATIENT_ID_KEY, connection.data.first().patientId)
                connection.data.first().glucoseItem.mapToGlucoseReading()
            }
            is ConnectionResponse.Error -> null
        }


        return currentReading
    }

    override suspend fun getGraphReadings(): List<GlucoseReading> {
        val pid = getPatientId() ?: return emptyList()
        val response = makeGetRequest<GraphResponse>("$CONNECTIONS_URL/$pid/graph") ?: return emptyList()

        return response.data.graphData.map { it.mapToGlucoseReading() }
    }

    private suspend inline fun <reified T> makeGetRequest(url: String): T? {
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

        return if(response.status.isSuccess()) {
            response.body()
        } else {
            null
        }
    }

    override suspend fun getPatientId(): String? {
        return settings.getStringOrNull(PATIENT_ID_KEY)
    }
}
