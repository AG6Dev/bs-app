package dev.ag6.libredesktop.ui.alarms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.ag6.libredesktop.model.alarms.AlarmSettings
import dev.ag6.libredesktop.ui.components.PreferenceRow
import dev.ag6.libredesktop.ui.components.ScreenHeader
import dev.ag6.libredesktop.ui.components.SectionCard
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch

object AlarmsScreen : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Alarm)
            return remember {
                TabOptions(2u, "Alarms", icon)
            }
        }

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<AlarmsScreenModel>()
        val state by screenModel.uiState.collectAsState()

        AlarmsScreenContent(
            state.alarmSettings,
            screenModel::onAlarmSettingsChanged,
        )
    }
}

@Composable
fun AlarmsScreenContent(
    settings: AlarmSettings,
    onSettingsChanged: (AlarmSettings) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    Surface {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                ScreenHeader(
                    eyebrow = "Notifications",
                    title = "Alarms",
                    subtitle = "Configure alarms to notify you if you're out of range"
                )
            }

            item {
                SectionCard(title = "Alarms", subtitle = "Enable or disable all alarm notifications") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PreferenceRow(
                            title = "Enable alarms", subtitle = "Send a notification when your glucose is out of range"
                        ) {
                            Checkbox(
                                checked = settings.alarmsEnabled,
                                onCheckedChange = { onSettingsChanged(settings.copy(alarmsEnabled = it)) },
                            )
                        }

                        AnimatedVisibility(visible = settings.alarmsEnabled) {
                            Column(
                                modifier = Modifier.padding(start = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                PreferenceRow(
                                    title = "Interval", subtitle = "Set the interval in which the alarm repeats"
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AlarmSettings.ALARM_INTERVALS.forEach { interval ->
                                            FilterChip(
                                                selected = settings.alarmInterval == interval,
                                                onClick = { onSettingsChanged(settings.copy(alarmInterval = interval)) },
                                                label = { Text("${interval}m") },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                                )
                                            )
                                        }
                                    }
                                }

                                PreferenceRow(
                                    title = "Enable desktop notifications",
                                    subtitle = "Show a desktop alert alongside alarm activity"
                                ) {
                                    Checkbox(
                                        checked = settings.notificationsEnabled,
                                        onCheckedChange = { onSettingsChanged(settings.copy(notificationsEnabled = it)) },
                                    )

                                }

                                PreferenceRow(
                                    title = "Enable sound", subtitle = "Play a sound when an alarm notification is sent"
                                ) {
                                    Checkbox(
                                        checked = settings.soundEnabled,
                                        onCheckedChange = { onSettingsChanged(settings.copy(soundEnabled = it)) },
                                    )
                                }

                                if (settings.soundEnabled) {
                                    OutlinedTextField(
                                        value = settings.customSoundPath ?: "",
                                        onValueChange = {},
                                        label = { Text("Custom sound path") },
                                        placeholder = { Text("Select a custom sound file") },
                                        readOnly = true,
                                        trailingIcon = {
                                            IconButton(
                                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = {
                                                    coroutineScope.launch {
                                                        val selectedFile = FileKit.openFilePicker(
                                                            FileKitType.File(
                                                                "mp3", "wav", "m4a"
                                                            )
                                                        )
                                                        selectedFile?.file?.let {
                                                            onSettingsChanged(
                                                                settings.copy(
                                                                    customSoundPath = it.absolutePath
                                                                )
                                                            )
                                                        }
                                                    }
                                                }) {
                                                Icon(
                                                    imageVector = Icons.Default.FolderOpen,
                                                    contentDescription = "Choose custom sound file",
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}