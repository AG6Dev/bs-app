package dev.ag6.libredesktop.repository.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.encodeValue
import dev.ag6.libredesktop.model.reading.ReadingUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
class SettingsRepositoryImpl(private val settings: Settings) : SettingsRepository {
    private val readingUnits = MutableStateFlow(
        settings.decodeValue<ReadingUnit>(Keys.READING_UNITS, ReadingUnit.MMOL)
    )

    override fun setReadingUnits(units: ReadingUnit) {
        settings.encodeValue(Keys.READING_UNITS, units)
        readingUnits.value = units
    }

    override fun getReadingUnits(): Flow<ReadingUnit> = readingUnits.asStateFlow()

    private object Keys {
        const val READING_UNITS = "reading_units"
    }
}
