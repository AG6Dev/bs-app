package dev.ag6.libredesktop.repository.auth

import com.github.javakeyring.Keyring
import com.russhwolf.settings.Settings
import dev.ag6.libredesktop.api.LibreApiCallResult
import dev.ag6.libredesktop.api.buildLibreApiUrl
import dev.ag6.libredesktop.api.executeLibreApiRequest
import dev.ag6.libredesktop.api.getLibreApiRegion
import dev.ag6.libredesktop.model.auth.AuthLoginData
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings,
    private val keyring: Keyring,
    private val json: Json
) : AuthRepository {
    companion object {
        private const val AUTH_PATH = "llu/auth/login"

        private const val USER_ID_KEY = "user_id"
        private const val USER_EMAIL_KEY = "user_email"
        private const val EXPIRY_KEY = "auth_token_expiry"
        private const val PATIENT_ID_KEY = "patient_id"

        private const val KEYRING_SERVICE = "LibreDesktop"
    }

    override suspend fun isAuthenticated(): Boolean {
        return getAuthToken() != null
    }

    override suspend fun getAuthToken(): String? {
        val email = getUserEmail() ?: return null
        val token = keyring.getPassword(KEYRING_SERVICE, email)

        return token?.takeIf { _ ->
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
        keyring.deletePassword(KEYRING_SERVICE, settings.getStringOrNull(USER_EMAIL_KEY) ?: return)
        settings.remove(USER_ID_KEY)
        settings.remove(USER_EMAIL_KEY)
        settings.remove(EXPIRY_KEY)
        settings.remove(PATIENT_ID_KEY)
    }

    override suspend fun login(
        username: String,
        password: String,
        countryCode: String
    ): LoginResult {
        val authResult = executeLibreApiRequest(
            settings = settings,
            json = json,
            initialRegion = settings.getLibreApiRegion() ?: countryCode.ifBlank { null },
            successSerializer = AuthLoginData.serializer()
        ) { region ->
            httpClient.post(buildLibreApiUrl(region, AUTH_PATH)) {
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
        }

        return when (authResult) {
            is LibreApiCallResult.Success -> {
                keyring.setPassword(KEYRING_SERVICE, authResult.data.user.email, authResult.data.authTicket.token)
                settings.putString(USER_ID_KEY, authResult.data.user.id)
                settings.putString(USER_EMAIL_KEY, authResult.data.user.email)
                settings.putLong(EXPIRY_KEY, authResult.data.authTicket.expires)
                LoginResult.Success(authResult.data)
            }

            is LibreApiCallResult.Failure -> {
                LoginResult.Failure(authResult.message ?: "Login failed.")
            }
        }
    }
}
