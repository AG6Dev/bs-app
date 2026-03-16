package dev.ag6.libredesktop.ui.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import dev.ag6.libredesktop.model.reading.GlucoseReading
import dev.ag6.libredesktop.model.reading.ReadingUnit
import dev.ag6.libredesktop.model.reading.TrendArrow
import dev.ag6.libredesktop.ui.screen.SettingsScreen
import org.jetbrains.compose.resources.painterResource
import java.text.SimpleDateFormat
import java.util.*

class OverviewScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<OverviewScreenModel>()
        val state by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.current

        OverviewScreenContent(
            isLoading = state.isLoading,
            currentReading = state.currentReading,
            pastReadings = state.graphData,
            readingUnit = state.readingUnit,
            onOpenSettings = { navigator?.push(SettingsScreen()) }
        )
    }
}

@Composable
private fun OverviewScreenContent(
    isLoading: Boolean,
    currentReading: GlucoseReading? = null,
    pastReadings: List<GlucoseReading> = listOf(),
    readingUnit: ReadingUnit,
    onOpenSettings: () -> Unit
) {
    if (isLoading && currentReading == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpenSettings) {
                    Text("Settings")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (currentReading != null) {
                CurrentReadingCard(currentReading, readingUnit)
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (pastReadings.isNotEmpty()) {
                GlucoseGraph(
                    readings = pastReadings,
                    currentReading = currentReading,
                    readingUnit = readingUnit,
                    modifier = Modifier.fillMaxWidth().height(250.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GlucoseGraph(
    readings: List<GlucoseReading>,
    currentReading: GlucoseReading? = null,
    readingUnit: ReadingUnit,
    modifier: Modifier = Modifier
) {
    val theme = MaterialTheme.colorScheme
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    val baseMinGlucose = 40f
    val baseMaxGlucose = 300f
    val targetMin = 70f
    val targetMax = 180f
    val displayedReadings = remember(readings, currentReading) {
        buildList {
            addAll(readings)
            if (currentReading != null && readings.none { it.timestamp == currentReading.timestamp }) {
                add(currentReading)
            }
        }.sortedBy { it.timestamp }
    }
    val (minGlucose, maxGlucose) = remember(displayedReadings) {
        calculateGraphBounds(
            readings = displayedReadings,
            baseMinGlucose = baseMinGlucose,
            baseMaxGlucose = baseMaxGlucose
        )
    }
    val currentReadingIndex = remember(displayedReadings, currentReading) {
        currentReading?.let { reading ->
            displayedReadings.indexOfLast { it.timestamp == reading.timestamp }.takeIf { it >= 0 }
        }
    }
    val graphPoints = remember(displayedReadings, canvasSize, minGlucose, maxGlucose) {
        if (canvasSize.width == 0 || canvasSize.height == 0) {
            emptyList()
        } else {
            calculateGraphPoints(
                readings = displayedReadings,
                width = canvasSize.width.toFloat(),
                height = canvasSize.height.toFloat(),
                minGlucose = minGlucose,
                maxGlucose = maxGlucose
            )
        }
    }

    Card(
        modifier = modifier, colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Canvas(
                modifier = Modifier.fillMaxSize().onSizeChanged { canvasSize = it }
                    .onPointerEvent(PointerEventType.Move) { event ->
                        hoveredIndex = graphPoints.indexOfNearestPoint(event.changes.first().position)
                    }
                    .onPointerEvent(PointerEventType.Exit) {
                        hoveredIndex = null
                    }
            ) {
                if (displayedReadings.size < 2) return@Canvas

                val range = maxGlucose - minGlucose

                val width = size.width
                val height = size.height

                // Draw target range background (70-180 mg/dL)
                val targetMinY = height - ((targetMin - minGlucose) / range * height)
                val targetMaxY = height - ((targetMax - minGlucose) / range * height)

                drawRect(
                    color = Color.Green.copy(alpha = 0.1f),
                    topLeft = Offset(0f, targetMaxY),
                    size = Size(width, targetMinY - targetMaxY)
                )

                val points = if (graphPoints.size == displayedReadings.size) {
                    graphPoints
                } else {
                    calculateGraphPoints(displayedReadings, width, height, minGlucose, maxGlucose)
                }

                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                drawPath(
                    path = path, color = theme.primary, style = Stroke(width = 3.dp.toPx())
                )

                // Draw points
                points.forEachIndexed { index, point ->
                    val isHovered = hoveredIndex == index
                    val isCurrentReading = currentReadingIndex == index
                    drawCircle(
                        color = when {
                            isHovered -> theme.tertiary
                            isCurrentReading -> theme.error
                            else -> theme.primary
                        }, radius = when {
                            isHovered -> 6.dp.toPx()
                            isCurrentReading -> 5.dp.toPx()
                            else -> 4.dp.toPx()
                        }, center = point
                    )
                }
            }

            if (canvasSize.height > 0) {
                TargetRangeLabel(
                    text = "High ${readingUnit.formatValueOnly(targetMax.toInt())}",
                    y = glucoseToGraphY(targetMax, canvasSize.height.toFloat(), minGlucose, maxGlucose),
                    canvasSize = canvasSize,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                TargetRangeLabel(
                    text = "Low ${readingUnit.formatValueOnly(targetMin.toInt())}",
                    y = glucoseToGraphY(targetMin, canvasSize.height.toFloat(), minGlucose, maxGlucose),
                    canvasSize = canvasSize,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }

            val selectedIndex = hoveredIndex
            if (selectedIndex != null && selectedIndex in displayedReadings.indices && selectedIndex in graphPoints.indices) {
                val selectedReading = displayedReadings[selectedIndex]
                val selectedPoint = graphPoints[selectedIndex]
                HoveredReadingInfo(
                    reading = selectedReading,
                    readingUnit = readingUnit,
                    point = selectedPoint,
                    canvasSize = canvasSize,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
        }
    }
}

@Composable
private fun TargetRangeLabel(
    text: String, y: Float, canvasSize: IntSize, modifier: Modifier = Modifier
) {
    val labelHeight = 24
    val x = 0
    val yOffset = (y.toInt() - (labelHeight / 2)).coerceIn(0, (canvasSize.height - labelHeight).coerceAtLeast(0))

    Surface(
        modifier = modifier.offset { IntOffset(x, yOffset) },
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
        tonalElevation = 2.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun HoveredReadingInfo(
    reading: GlucoseReading,
    readingUnit: ReadingUnit,
    point: Offset,
    canvasSize: IntSize,
    modifier: Modifier = Modifier
) {
    val timestamp = remember(reading.timestamp) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(reading.timestamp))
    }
    val tooltipWidth = 148
    val tooltipHeight = 60
    val horizontalPadding = 12
    val verticalPadding = 12
    val tooltipX =
        (point.x.toInt() - (tooltipWidth / 2)).coerceIn(0, (canvasSize.width - tooltipWidth).coerceAtLeast(0))
    val preferredY = point.y.toInt() - tooltipHeight - verticalPadding
    val fallbackY = point.y.toInt() + verticalPadding
    val tooltipY =
        if (preferredY >= 0) preferredY else fallbackY.coerceAtMost((canvasSize.height - tooltipHeight).coerceAtLeast(0))

    Surface(
        modifier = modifier.offset { IntOffset(tooltipX + horizontalPadding, tooltipY + verticalPadding) },
        shape = MaterialTheme.shapes.small,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = readingUnit.format(reading.valueInMgPerDl), style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = timestamp, style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun calculateGraphPoints(
    readings: List<GlucoseReading>, width: Float, height: Float, minGlucose: Float, maxGlucose: Float
): List<Offset> {
    if (readings.size < 2 || width <= 0f || height <= 0f) return emptyList()

    val range = maxGlucose - minGlucose
    return readings.mapIndexed { index, reading ->
        val x = (index.toFloat() / (readings.size - 1)) * width
        val glucose = reading.valueInMgPerDl.toFloat().coerceIn(minGlucose, maxGlucose)
        val y = height - ((glucose - minGlucose) / range * height)
        Offset(x, y)
    }
}

private fun calculateGraphBounds(
    readings: List<GlucoseReading>, baseMinGlucose: Float, baseMaxGlucose: Float
): Pair<Float, Float> {
    if (readings.isEmpty()) return baseMinGlucose to baseMaxGlucose

    val maxReading = readings.maxOf { it.valueInMgPerDl.toFloat() }
    val paddedMax = maxReading + 20f
    val dynamicMax = maxOf(baseMaxGlucose, paddedMax)

    return baseMinGlucose to dynamicMax
}

private fun glucoseToGraphY(glucose: Float, height: Float, minGlucose: Float, maxGlucose: Float): Float {
    val range = maxGlucose - minGlucose
    val boundedGlucose = glucose.coerceIn(minGlucose, maxGlucose)
    return height - ((boundedGlucose - minGlucose) / range * height)
}

private fun List<Offset>.indexOfNearestPoint(pointerPosition: Offset, maxDistancePx: Float = 12f): Int? {
    if (isEmpty()) return null

    val maxDistanceSquared = maxDistancePx * maxDistancePx
    var nearestIndex: Int? = null
    var nearestDistanceSquared = Float.MAX_VALUE

    forEachIndexed { index, point ->
        val deltaX = point.x - pointerPosition.x
        val deltaY = point.y - pointerPosition.y
        val distanceSquared = (deltaX * deltaX) + (deltaY * deltaY)
        if (distanceSquared <= maxDistanceSquared && distanceSquared < nearestDistanceSquared) {
            nearestIndex = index
            nearestDistanceSquared = distanceSquared
        }
    }

    return nearestIndex
}

@Composable
fun CurrentReadingCard(reading: GlucoseReading, readingUnit: ReadingUnit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current Reading", style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = readingUnit.formatValueOnly(reading.valueInMgPerDl),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.weight(1f)
                )

                val trend = TrendArrow.entries.find { it.value == reading.trendArrow }
                if (trend != null) {
                    Image(
                        painter = painterResource(trend.imageVector),
                        contentDescription = "Trend Arrow",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = readingUnit.label,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date(reading.timestamp)
            Text(
                text = "at ${sdf.format(date)}", style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
