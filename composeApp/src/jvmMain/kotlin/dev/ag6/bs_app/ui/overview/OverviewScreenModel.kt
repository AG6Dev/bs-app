package dev.ag6.bs_app.ui.overview

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.ag6.bs_app.repository.readings.ReadingsRepository
import dev.ag6.bs_app.util.scheduleRepeatingTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class OverviewScreenModel(repository: ReadingsRepository) : ScreenModel {
    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isLoading = true) }

        screenModelScope.launch {
            scheduleRepeatingTask(1.minutes) {
                val currentReading = repository.getCurrentReading()
                val graphData = repository.getGraphReadings()
                _uiState.update { it.copy(isLoading = false, currentReading = currentReading, graphData = graphData) }
                println("Fetching current reading: $currentReading")
            }
        }
    }
}