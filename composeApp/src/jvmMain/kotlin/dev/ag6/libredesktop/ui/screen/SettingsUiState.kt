package dev.ag6.libredesktop.ui.screen

import dev.ag6.libredesktop.model.reading.ReadingUnit
import dev.ag6.libredesktop.model.theme.ThemeMode

data class SettingsUiState(
    val isLoading: Boolean = true,
    val readingUnit: ReadingUnit = ReadingUnit.MMOL,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val highTargetMgDl: Int = 180,
    val lowTargetMgDl: Int = 70,
    val email: String? = null
)
