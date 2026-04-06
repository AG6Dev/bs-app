package dev.ag6.libredesktop.ui.alarms

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.ag6.libredesktop.model.alarms.AlarmSettings
import dev.ag6.libredesktop.repository.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmsScreenModel(private val settings: SettingsRepository) : ScreenModel {
    private val _uiState = MutableStateFlow(AlarmsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        screenModelScope.launch {
            settings.getAlarmSettings().collect { settings ->
                _uiState.update {
                    it.copy(alarmSettings = settings)
                }
            }
        }
    }

    fun onAlarmSettingsChanged(newValue: AlarmSettings) {
        screenModelScope.launch {
            settings.setAlarmSettings(newValue)
            _uiState.update { it.copy(alarmSettings = newValue) }
        }
    }
}