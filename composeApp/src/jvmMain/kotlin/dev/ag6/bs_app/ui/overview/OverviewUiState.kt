package dev.ag6.bs_app.ui.overview

import dev.ag6.bs_app.model.reading.GlucoseReading

data class OverviewUiState(
    val isLoading: Boolean = false,
    val graphData: List<GlucoseReading> = listOf(),
    val currentReading: GlucoseReading? = null
)