package dev.ag6.libredesktop.repository.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getIntFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.set
import dev.ag6.libredesktop.model.reading.ReadingUnit
import dev.ag6.libredesktop.model.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
class SettingsRepositoryImpl(private val settings: ObservableSettings) : SettingsRepository {
    override suspend fun setReadingUnits(units: ReadingUnit) {
        settings[Keys.READING_UNITS_KEY] = units.name
    }

    override fun getReadingUnits(): Flow<ReadingUnit> {
        return settings.getStringOrNullFlow(Keys.READING_UNITS_KEY)
            .map { ReadingUnit.entries.firstOrNull { entry -> entry.name == it } ?: ReadingUnit.MMOL }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        settings[Keys.THEME_MODE_KEY] = themeMode.name
    }

    override fun getThemeMode(): Flow<ThemeMode> {
        return settings.getStringOrNullFlow(Keys.THEME_MODE_KEY)
            .map { storedValue -> ThemeMode.entries.firstOrNull { it.name == storedValue } ?: ThemeMode.SYSTEM }
    }

    override suspend fun setHighTarget(valueMgDl: Int) {
        settings[Keys.HIGH_TARGET_KEY] = valueMgDl
    }

    override fun getHighTarget(): Flow<Int> {
        return settings.getIntFlow(Keys.HIGH_TARGET_KEY, defaultValue = 180)
    }

    override suspend fun setLowTarget(valueMgDl: Int) {
        settings[Keys.LOW_TARGET_KEY] = valueMgDl
    }

    override fun getLowTarget(): Flow<Int> {
        return settings.getIntFlow(Keys.LOW_TARGET_KEY, defaultValue = 70)
    }

    private object Keys {
        const val READING_UNITS_KEY = "reading_units"
        const val THEME_MODE_KEY = "theme_mode"
        const val HIGH_TARGET_KEY = "high_target"
        const val LOW_TARGET_KEY = "low_target"
    }
}
