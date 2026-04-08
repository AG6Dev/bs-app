package dev.ag6.libredesktop.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import dev.ag6.libredesktop.model.reading.GlucoseReading
import dev.ag6.libredesktop.model.reading.ReadingUnit
import dev.ag6.libredesktop.model.reading.TrendArrow
import dev.ag6.libredesktop.model.reading.TrendArrow.Companion.trendArrowFromValue
import dev.ag6.libredesktop.ui.components.GlucoseGraphView
import dev.ag6.libredesktop.ui.components.SectionCard
import dev.ag6.libredesktop.ui.theme.statusHigh
import dev.ag6.libredesktop.ui.theme.statusInRange
import dev.ag6.libredesktop.ui.theme.statusLow
import org.jetbrains.compose.resources.painterResource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object OverviewScreen : Tab {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<OverviewScreenModel>()
        val state by screenModel.uiState.collectAsState()

        OverviewScreenContent(
            isLoading = state.isLoading,
            currentReading = state.currentReading,
            pastReadings = state.graphData,
            readingUnit = state.readingUnit,
            lowTargetMgDl = state.lowTargetMgDl,
            highTargetMgDl = state.highTargetMgDl,
        )
    }

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Dashboard)
            return remember {
                TabOptions(0u, "Dashboard", icon)
            }
        }
}

@Composable
private fun OverviewScreenContent(
    isLoading: Boolean,
    currentReading: GlucoseReading? = null,
    pastReadings: List<GlucoseReading> = listOf(),
    readingUnit: ReadingUnit,
    lowTargetMgDl: Int,
    highTargetMgDl: Int,
) {
    if (isLoading && currentReading == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val lowTarget = remember(lowTargetMgDl, readingUnit) {
            when (readingUnit) {
                ReadingUnit.MMOL -> lowTargetMgDl / 18.0
                ReadingUnit.MGDL -> lowTargetMgDl.toDouble()
            }
        }
        val highTarget = remember(highTargetMgDl, readingUnit) {
            when (readingUnit) {
                ReadingUnit.MMOL -> highTargetMgDl / 18.0
                ReadingUnit.MGDL -> highTargetMgDl.toDouble()
            }
        }

        val chartModel = remember(pastReadings, currentReading, readingUnit) {
            val readings =
                if (currentReading != null && pastReadings.none { it.timestamp == currentReading.timestamp }) {
                    (pastReadings + currentReading).sortedBy { it.timestamp }
                } else {
                    pastReadings
                }
            readings
                .takeIf { it.isNotEmpty() }
                ?.let { readings ->
                    CartesianChartModel(
                        LineCartesianLayerModel.build {
                            series(
                                x = readings.map { it.timestamp.toDouble() },
                                y = readings.map { reading ->
                                    when (readingUnit) {
                                        ReadingUnit.MMOL -> reading.valueInMgPerDl / 18.0
                                        ReadingUnit.MGDL -> reading.valueInMgPerDl.toDouble()
                                    }
                                }
                            )
                        }
                    )
                }
        }

        Scaffold {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionCard(
                        title = "Current glucose",
                    ) {
                        val statusColor = currentReading?.let {
                            glucoseStatusColor(
                                valueInMgPerDl = it.valueInMgPerDl,
                                lowTargetMgDl = lowTargetMgDl,
                                highTargetMgDl = highTargetMgDl
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = currentReading?.let { readingUnit.format(it.valueInMgPerDl) } ?: "--",
                                    style = MaterialTheme.typography.headlineMedium)
                                currentReading?.trendArrow?.let(::trendArrowFromValue)?.let { trendArrow ->
                                    TrendArrowBadge(
                                        trendArrow = trendArrow,
                                        backgroundColor = statusColor ?: MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                            currentReading?.let { reading ->
                                val time = remember(reading.timestamp) {
                                    Instant.ofEpochMilli(reading.timestamp).atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                                }
                                Text(
                                    text = "Updated $time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                if (chartModel != null) {
                    SectionCard(
                        title = "Glucose history",
                        subtitle = "Recent readings in ${readingUnit.label}. Targets: ${
                            readingUnit.formatValueOnly(
                                lowTargetMgDl
                            )
                        } to ${readingUnit.formatValueOnly(highTargetMgDl)}."
                    ) {
                        GlucoseGraphView(
                            chartModel = chartModel,
                            lowTarget = lowTarget,
                            highTarget = highTarget,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text("No glucose history available yet.")
                }
            }
        }
    }
}

@Composable
private fun TrendArrowBadge(
    trendArrow: TrendArrow,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(trendArrow.imageVector),
            contentDescription = trendArrow.name,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun glucoseStatusColor(
    valueInMgPerDl: Int,
    lowTargetMgDl: Int,
    highTargetMgDl: Int
): Color = when {
    valueInMgPerDl < lowTargetMgDl -> statusLow
    valueInMgPerDl > highTargetMgDl -> statusHigh
    else -> statusInRange
}