package dev.ag6.libredesktop.repository.auth

import com.russhwolf.settings.Settings
import dev.ag6.libredesktop.api.LibreApiResponse
import dev.ag6.libredesktop.api.decodeLibreApiResponse
import dev.ag6.libredesktop.model.auth.AuthLoginData
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings,
    private val json: Json
) : AuthRepository {
    companion object {
        private const val AUTH_ENDPOINT = "https://api.libreview.io/llu/auth/login"

        private const val TOKEN_KEY = "auth_token"
        private const val USER_ID_KEY = "user_id"
        private const val EXPIRY_KEY = "auth_token_expiry"
    }

    override suspend fun isAuthenticated(): Boolean {
        return getAuthToken() != null
    }

    override suspend fun getAuthToken(): String? {
        return settings.getStringOrNull(TOKEN_KEY)?.takeIf { _ ->
            val expiry = settings.getLongOrNull(EXPIRY_KEY) ?: return@takeIf false
            val currentTime = System.currentTimeMillis() / 1000
            currentTime < expiry
        }
    }

    override suspend fun getUserId(): String? {
        return settings.getStringOrNull(USER_ID_KEY)
    }

    override fun login(
        username: String,
        password: String,
        countryCode: String
    ): Flow<LibreApiResponse<AuthLoginData>> = flow {
        val response = httpClient.post(AUTH_ENDPOINT) {
            contentType(ContentType.Application.Json)
            headers {
                append("product", "llu.android")
                append("version", "4.2.1")
            }
            setBody(
                mapOf(
                    "email" to username,
                    "password" to password
                )
            )
        }

        val authResponse = decodeLibreApiResponse(
            response.bodyAsText(),
            AuthLoginData.serializer(),
            json
        )

        if (response.status == HttpStatusCode.OK && authResponse is LibreApiResponse.Success) {
            settings.putString(TOKEN_KEY, authResponse.data.authTicket.token)
            settings.putString(USER_ID_KEY, authResponse.data.user.id)
            settings.putLong(EXPIRY_KEY, authResponse.data.authTicket.expires)
        }

        emit(authResponse)
    }
}
