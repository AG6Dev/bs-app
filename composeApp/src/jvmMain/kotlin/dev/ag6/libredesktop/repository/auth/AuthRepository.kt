package dev.ag6.libredesktop.repository.auth

import dev.ag6.libredesktop.api.LibreApiResponse
import dev.ag6.libredesktop.model.auth.AuthLoginData
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun isAuthenticated(): Boolean

    suspend fun getAuthToken(): String?

    suspend fun getUserId(): String?

    fun login(username: String, password: String, countryCode: String = ""): Flow<LibreApiResponse<AuthLoginData>>
}
