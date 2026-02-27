package dev.ag6.bs_app.screen.auth

import dev.ag6.bs_app.model.auth.AuthResponse

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val data: AuthResponse? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
)