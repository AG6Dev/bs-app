package dev.ag6.libredesktop.repository.auth

import com.russhwolf.settings.Settings
import dev.ag6.libredesktop.api.*
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
        private const val AUTH_PATH = "llu/auth/login"

        private const val TOKEN_KEY = "auth_token"
        private const val USER_ID_KEY = "user_id"
        private const val USER_EMAIL_KEY = "user_email"
        private const val EXPIRY_KEY = "auth_token_expiry"
        private const val PATIENT_ID_KEY = "patient_id"
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

    override suspend fun getUserEmail(): String? {
        return settings.getStringOrNull(USER_EMAIL_KEY)
    }

    override suspend fun logout() {
        settings.remove(TOKEN_KEY)
        settings.remove(USER_ID_KEY)
        settings.remove(USER_EMAIL_KEY)
        settings.remove(EXPIRY_KEY)
        settings.remove(PATIENT_ID_KEY)
    }

    override fun login(
        username: String,
        password: String,
        countryCode: String
    ): Flow<LibreApiResponse<AuthLoginData>> = flow {
        val authResponse = loginAgainstRegion(
            username = username,
            password = password,
            region = settings.getLibreApiRegion() ?: countryCode.ifBlank { null }
        )

        val resolvedResponse = if (authResponse is LibreApiResponse.Redirect) {
            settings.setLibreApiRegion(authResponse.region)
            loginAgainstRegion(
                username = username,
                password = password,
                region = authResponse.region
            )
        } else {
            authResponse
        }

        if (resolvedResponse is LibreApiResponse.Success) {
            settings.putString(TOKEN_KEY, resolvedResponse.data.authTicket.token)
            settings.putString(USER_ID_KEY, resolvedResponse.data.user.id)
            settings.putString(USER_EMAIL_KEY, resolvedResponse.data.user.email)
            settings.putLong(EXPIRY_KEY, resolvedResponse.data.authTicket.expires)
        }

        emit(resolvedResponse)
    }

    private suspend fun loginAgainstRegion(
        username: String,
        password: String,
        region: String?
    ): LibreApiResponse<AuthLoginData> {
        val response = httpClient.post(buildLibreApiUrl(region, AUTH_PATH)) {
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

        if (authResponse is LibreApiResponse.Success && !region.isNullOrBlank()) {
            settings.setLibreApiRegion(region)
        }

        return authResponse
    }
}
