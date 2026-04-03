package dev.ag6.libredesktop.ui.overview

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.ag6.libredesktop.repository.readings.ReadingsRepository
import dev.ag6.libredesktop.repository.settings.SettingsRepository
import dev.ag6.libredesktop.util.scheduleRepeatingTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class OverviewScreenModel(
    repository: ReadingsRepository,
    settingsRepository: SettingsRepository
) : ScreenModel {
    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isLoading = true) }

        screenModelScope.launch {
            settingsRepository.getReadingUnits().collect { readingUnit ->
                _uiState.update { it.copy(readingUnit = readingUnit) }
            }
        }

        screenModelScope.launch {
            settingsRepository.getHighTarget().collect { highTargetMgDl ->
                _uiState.update { it.copy(highTargetMgDl = highTargetMgDl) }
            }
        }

        screenModelScope.launch {
            settingsRepository.getLowTarget().collect { lowTargetMgDl ->
                _uiState.update { it.copy(lowTargetMgDl = lowTargetMgDl) }
            }
        }

        screenModelScope.launch {
            scheduleRepeatingTask(1.minutes) {
                val currentReading = repository.getCurrentReading()
                val graphData = repository.getGraphReadings()
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        currentReading = currentReading,
                        graphData = graphData
                    )
                }
            }
        }
    }
}
