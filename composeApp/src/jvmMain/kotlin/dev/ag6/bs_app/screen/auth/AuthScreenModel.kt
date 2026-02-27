package dev.ag6.bs_app.screen.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.ag6.bs_app.model.auth.AuthResponse
import dev.ag6.bs_app.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthScreenModel(
    private val repository: AuthRepository
) : ScreenModel {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isLoading = true) }

        screenModelScope.launch {
            val isLoggedIn = repository.isAuthenticated()
            _uiState.update {
                it.copy(
                    isLoading = false, isAuthenticated = isLoggedIn
                )
            }
        }
    }

    fun onLoginButtonPressed(username: String, password: String) {
        screenModelScope.launch {
            repository.login(username, password).collect { result ->
                when(result) {
                    is AuthResponse.Login -> {
                        _uiState.update { it.copy(isAuthenticated = true, data = result) }
                    }
                    is AuthResponse.Redirect -> {
                        _uiState.update { it.copy(isAuthenticated = false, data = result) }
                    }
                    is AuthResponse.Error -> {
                        _uiState.update { it.copy(isAuthenticated = false, error = result.error.message) }
                    }
                }
            }
        }
    }
}