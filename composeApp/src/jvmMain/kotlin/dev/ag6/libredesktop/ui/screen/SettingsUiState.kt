package dev.ag6.libredesktop.ui.screen

import dev.ag6.libredesktop.model.reading.ReadingUnit

data class SettingsUiState(
    val isLoading: Boolean = true,
    val readingUnit: ReadingUnit = ReadingUnit.MMOL,
    val email: String? = null
)
