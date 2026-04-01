package dev.ag6.libredesktop.ui.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.ag6.libredesktop.repository.auth.AuthRepository
import dev.ag6.libredesktop.repository.auth.LoginResult
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
        _uiState.update { it.copy(isLoading = true, error = null) }

        screenModelScope.launch {
            when (val result = repository.login(username, password)) {
                is LoginResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                }

                is LoginResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
}
