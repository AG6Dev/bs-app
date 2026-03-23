package dev.ag6.libredesktop.repository.auth

interface AuthRepository {
    suspend fun isAuthenticated(): Boolean

    suspend fun getAuthToken(): String?

    suspend fun getUserId(): String?

    suspend fun getUserEmail(): String?

    suspend fun logout()

    suspend fun login(username: String, password: String, countryCode: String = ""): LoginResult
}
