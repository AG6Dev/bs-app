package dev.ag6.libredesktop.ui.overview

import dev.ag6.libredesktop.model.reading.GlucoseReading
import dev.ag6.libredesktop.model.reading.ReadingUnit

data class OverviewUiState(
    val isLoading: Boolean = false,
    val graphData: List<GlucoseReading> = listOf(),
    val currentReading: GlucoseReading? = null,
    val readingUnit: ReadingUnit = ReadingUnit.MMOL
)
