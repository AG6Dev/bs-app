package dev.ag6.bs_app.repository

import com.russhwolf.settings.Settings
import dev.ag6.bs_app.model.auth.AuthResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings
) : AuthRepository {
    companion object {
        private const val AUTH_ENDPOINT = "https://api.libreview.io/llu/auth/login"
        private const val TOKEN_KEY = "auth_token"
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

    override fun login(
        username: String,
        password: String,
        countryCode: String
    ): Flow<AuthResponse> = flow {
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

        if (response.status == HttpStatusCode.OK) {
            val res : String = response.bodyAsText()
            println(res)
            val authResponse: AuthResponse = response.body()
            if (authResponse is AuthResponse.Login) {
                settings.putString(TOKEN_KEY, authResponse.data.authTicket.token)
                settings.putLong(EXPIRY_KEY, authResponse.data.authTicket.expires)
            }
            emit(authResponse)
        } else {
            // Handle other statuses if necessary, or throw exception
            // For now, we emit what we get if it's a valid AuthResponse type
            try {
                val authResponse: AuthResponse = response.body()
                emit(authResponse)
            } catch (e: Exception) {
                throw e
            }
        }
    }
}
