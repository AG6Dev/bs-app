package dev.ag6.libredesktop.repository.settings

import dev.ag6.libredesktop.model.reading.ReadingUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun setReadingUnits(units: ReadingUnit)
    fun getReadingUnits(): Flow<ReadingUnit>
}