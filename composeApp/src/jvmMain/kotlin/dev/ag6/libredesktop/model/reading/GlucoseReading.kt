package dev.ag6.libredesktop.model.reading

import dev.ag6.libredesktop.model.connection.GlucoseItem
import kotlinx.serialization.Serializable
import libredesktop.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

enum class TrendArrow(val value: Int, val imageVector: DrawableResource, val emoji: String) {
    RapidlyFalling(1, Res.drawable.south_24px, "⬇️"), Falling(2, Res.drawable.south_east_24px, "↘️"), Flat(
        3, Res.drawable.east_24px, "➡️"
    ),
    Rising(4, Res.drawable.north_east_24px, "↗️"), RapidlyRising(5, Res.drawable.north_24px, "⬆️");

    companion object {
        fun trendArrowFromValue(value: Int): TrendArrow? = TrendArrow.entries.firstOrNull { it.value == value }
    }
}


@Serializable
data class GlucoseReading(
    val timestamp: Long, val valueInMgPerDl: Int, val trendArrow: Int?
)

fun GlucoseItem.mapToGlucoseReading(): GlucoseReading {
    val dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a").withLocale(Locale.US)
    val localDateTime = LocalDateTime.parse(this.timestamp, dateFormatter)

    val convertedTimestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    return GlucoseReading(
        timestamp = convertedTimestamp, valueInMgPerDl = this.valueInMgPerDl, trendArrow = this.trendArrow
    )
}




