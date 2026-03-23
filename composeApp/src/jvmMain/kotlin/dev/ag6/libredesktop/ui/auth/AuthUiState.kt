package dev.ag6.libredesktop.ui.auth

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
)
