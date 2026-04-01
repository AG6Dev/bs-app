package dev.ag6.libredesktop.ui.screen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.ag6.libredesktop.model.reading.ReadingUnit
import dev.ag6.libredesktop.model.theme.ThemeMode
import dev.ag6.libredesktop.repository.auth.AuthRepository
import dev.ag6.libredesktop.repository.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsScreenModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ScreenModel {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        screenModelScope.launch {
            val email = authRepository.getUserEmail()
            _uiState.update { it.copy(email = email) }
        }

        screenModelScope.launch {
            settingsRepository.getReadingUnits().collect { readingUnit ->
                _uiState.update {
                    it.copy(
                        readingUnit = readingUnit
                    )
                }
            }
        }

        screenModelScope.launch {
            settingsRepository.getThemeMode().collect { themeMode ->
                _uiState.update {
                    it.copy(
                        themeMode = themeMode
                    )
                }
            }
        }

        screenModelScope.launch {
            settingsRepository.getHighTarget().collect { highTargetMgDl ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        highTargetMgDl = highTargetMgDl
                    )
                }
            }
        }

        screenModelScope.launch {
            settingsRepository.getLowTarget().collect { lowTargetMgDl ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lowTargetMgDl = lowTargetMgDl
                    )
                }
            }
        }
    }

    fun onReadingUnitSelected(readingUnit: ReadingUnit) {
        screenModelScope.launch {
            settingsRepository.setReadingUnits(readingUnit)
            _uiState.update {
                it.copy(readingUnit = readingUnit)
            }
        }
    }

    fun onThemeModeSelected(themeMode: ThemeMode) {
        screenModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
            _uiState.update {
                it.copy(themeMode = themeMode)
            }
        }
    }

    fun onTargetsSaved(lowTargetMgDl: Int, highTargetMgDl: Int) {
        screenModelScope.launch {
            settingsRepository.setLowTarget(lowTargetMgDl)
            settingsRepository.setHighTarget(highTargetMgDl)
            _uiState.update {
                it.copy(
                    lowTargetMgDl = lowTargetMgDl,
                    highTargetMgDl = highTargetMgDl
                )
            }
        }
    }

    fun onLogout(onLoggedOut: () -> Unit) {
        screenModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
