package dev.ag6.libredesktop.ui.auth

import dev.ag6.libredesktop.api.LibreApiResponse
import dev.ag6.libredesktop.model.auth.AuthLoginData

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val data: LibreApiResponse<AuthLoginData>? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
)
