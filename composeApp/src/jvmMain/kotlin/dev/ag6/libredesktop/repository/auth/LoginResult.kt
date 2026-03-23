package dev.ag6.libredesktop.repository.auth

import dev.ag6.libredesktop.model.auth.AuthLoginData

sealed interface LoginResult {
    data class Success(val data: AuthLoginData) : LoginResult
    data class Failure(val message: String) : LoginResult
}
