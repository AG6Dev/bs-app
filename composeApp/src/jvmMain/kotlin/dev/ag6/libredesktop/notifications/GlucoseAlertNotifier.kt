package dev.ag6.libredesktop.notifications

import com.mmk.kmpnotifier.notification.NotifierManager
import dev.ag6.libredesktop.AppContext
import dev.ag6.libredesktop.model.alarms.AlarmSettings
import dev.ag6.libredesktop.model.reading.GlucoseReading
import dev.ag6.libredesktop.model.reading.ReadingUnit
import dev.ag6.libredesktop.model.reading.TrendArrow.Companion.trendArrowFromValue
import dev.ag6.libredesktop.repository.settings.SettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import java.awt.Toolkit
import java.io.File
import javax.sound.sampled.AudioSystem
import kotlin.time.Duration.Companion.minutes

//TODO: if the user either presses on the notification, or opens the app while out of range at least once, disable the notifications
class GlucoseAlertNotifier(
    private val appContext: AppContext,
    private val settingsRepository: SettingsRepository,
) : AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val notifier = NotifierManager.getLocalNotifier()
    private var lastAlertTime: Long = 0L

    init {
        scope.launch {
            combine(
                appContext.currentReading,
                settingsRepository.getAlarmSettings(),
                settingsRepository.getLowTarget(),
                settingsRepository.getHighTarget(),
                settingsRepository.getReadingUnits(),
            ) { reading, alarmSettings, lowTarget, highTarget, readingUnit ->
                AlarmCheckData(reading, alarmSettings, lowTarget, highTarget, readingUnit)
            }.collect { checkAndFire(it) }
        }
    }

    private fun checkAndFire(data: AlarmCheckData) {
        val (reading, settings, lowTarget, highTarget) = data
        if (!settings.alarmsEnabled || reading == null) return

        val outOfRange = reading.valueInMgPerDl !in lowTarget..highTarget
        if (!outOfRange) return

        val now = System.currentTimeMillis()
        if (now - lastAlertTime < settings.alarmInterval.minutes.inWholeMilliseconds) return
        lastAlertTime = now

        val formattedValue = data.readingUnit.format(reading.valueInMgPerDl)
        var message = if (reading.valueInMgPerDl < lowTarget) {
            "Glucose LOW: $formattedValue"
        } else {
            "Glucose HIGH: $formattedValue"
        }
        message += "\nTrending: ${data.reading?.trendArrow?.let(::trendArrowFromValue).let { it?.emoji }}"

        notifier.notify {
            title = "Glucose Alert"
            body = message

        }

        if (settings.soundEnabled) {
            scope.launch(Dispatchers.IO) { playSound(settings.customSoundPath) }
        }
    }

    private suspend fun playSound(path: String?) = withContext(Dispatchers.IO) {
        if (path == null) {
            //TODO: Add a custom sound instead of the default beep
            Toolkit.getDefaultToolkit().beep()
            return@withContext
        }
        try {
            val stream = AudioSystem.getAudioInputStream(File(path))
            val clip = AudioSystem.getClip()
            clip.open(stream)
            clip.start()
            delay(clip.microsecondLength / 1000 + 200)
            clip.close()
        } catch (_: Exception) {
        }
    }

    override fun close() {
        scope.cancel()
    }

    private data class AlarmCheckData(
        val reading: GlucoseReading?,
        val alarmSettings: AlarmSettings,
        val lowTarget: Int,
        val highTarget: Int,
        val readingUnit: ReadingUnit,
    )
}
