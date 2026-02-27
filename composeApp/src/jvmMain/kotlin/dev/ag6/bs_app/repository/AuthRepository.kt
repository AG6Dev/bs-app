package dev.ag6.bs_app.repository

import dev.ag6.bs_app.model.auth.AuthResponse
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun isAuthenticated(): Boolean

    suspend fun getAuthToken(): String?

    fun login(username: String, password: String, countryCode: String = ""): Flow<AuthResponse>
}