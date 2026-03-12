package dev.ag6.libredesktop.ui.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.ag6.libredesktop.api.LibreApiResponse
import dev.ag6.libredesktop.repository.auth.AuthRepository
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.login(username, password).collect { result ->
                when(result) {
                    is LibreApiResponse.Success -> {
                        _uiState.update { it.copy(isLoading = false, isAuthenticated = true, data = result) }
                    }

                    is LibreApiResponse.Redirect -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                data = result,
                                error = "LibreView requested a region redirect to ${result.region}."
                            )
                        }
                    }

                    is LibreApiResponse.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                data = result,
                                error = result.message?.message ?: result.error?.message ?: "Login failed."
                            )
                        }
                    }
                }
            }
        }
    }
}
