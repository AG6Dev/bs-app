package dev.ag6.libredesktop.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import dev.ag6.libredesktop.model.reading.ReadingUnit
import dev.ag6.libredesktop.model.theme.ThemeMode
import dev.ag6.libredesktop.ui.auth.AuthScreen
import dev.ag6.libredesktop.ui.components.AppScreen
import dev.ag6.libredesktop.ui.components.PreferenceRow
import dev.ag6.libredesktop.ui.components.ScreenHeader
import dev.ag6.libredesktop.ui.components.SectionCard

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SettingsScreenModel>()
        val state by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.current

        SettingsScreenContent(
            state = state,
            onBack = { navigator?.pop() },
            onThemeModeSelected = screenModel::onThemeModeSelected,
            onReadingUnitSelected = screenModel::onReadingUnitSelected,
            onTargetsSaved = screenModel::onTargetsSaved,
            onLogout = {
                screenModel.onLogout {
                    navigator?.replaceAll(AuthScreen())
                }
            }
        )
    }
}

@Composable
private fun SettingsScreenContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onReadingUnitSelected: (ReadingUnit) -> Unit,
    onTargetsSaved: (Int, Int) -> Unit,
    onLogout: () -> Unit
) {
    var lowTargetInput by remember(state.readingUnit) { mutableStateOf("") }
    var highTargetInput by remember(state.readingUnit) { mutableStateOf("") }
    var targetError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.lowTargetMgDl, state.highTargetMgDl, state.readingUnit) {
        lowTargetInput = state.readingUnit.toDisplayValue(state.lowTargetMgDl)
        highTargetInput = state.readingUnit.toDisplayValue(state.highTargetMgDl)
        targetError = null
    }

    AppScreen(scrollable = true) {
        ScreenHeader(
            eyebrow = "Preferences",
            title = "Settings",
            subtitle = "Control appearance, units, target ranges, and account actions.",
            trailing = {
                OutlinedButton(onClick = onBack) {
                    Text("Back")
                }
            }
        )

        SectionCard(
            title = "Appearance",
            subtitle = "Choose how the app should render across desktop sessions."
        ) {
            PreferenceRow(
                title = "Theme mode",
                subtitle = "System follows your OS preference when available."
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.themeMode == mode,
                            onClick = { onThemeModeSelected(mode) },
                            label = { Text(mode.label) }
                        )
                    }
                }
            }
        }

        SectionCard(
            title = "Glucose units",
            subtitle = "Switch between mmol/L and mg/dL for readings and targets."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ReadingUnit.entries.forEach { unit ->
                    ReadingUnitRow(
                        unit = unit,
                        selected = state.readingUnit == unit,
                        onSelected = { onReadingUnitSelected(unit) }
                    )
                }
            }
        }

        SectionCard(
            title = "Target range",
            subtitle = "These values drive the shaded target band in the trend chart."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = lowTargetInput,
                    onValueChange = {
                        lowTargetInput = it
                        targetError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Low target") },
                    suffix = { Text(state.readingUnit.label) }
                )
                OutlinedTextField(
                    value = highTargetInput,
                    onValueChange = {
                        highTargetInput = it
                        targetError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("High target") },
                    suffix = { Text(state.readingUnit.label) }
                )
                targetError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = {
                        val lowTargetMgDl = state.readingUnit.parseDisplayValue(lowTargetInput)
                        val highTargetMgDl = state.readingUnit.parseDisplayValue(highTargetInput)
                        if (lowTargetMgDl == null || highTargetMgDl == null) {
                            targetError = "Enter valid low and high target values."
                        } else {
                            val validationError = when {
                                lowTargetMgDl < 40 || highTargetMgDl > 400 -> {
                                    "Targets must stay within 40 to 400 mg/dL."
                                }

                                lowTargetMgDl >= highTargetMgDl -> {
                                    "Low target must be below high target."
                                }

                                else -> null
                            }

                            targetError = validationError

                            if (validationError == null) {
                                onTargetsSaved(lowTargetMgDl, highTargetMgDl)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save target range")
                }
            }
        }

        SectionCard(
            title = "Account",
            subtitle = "Session and identity controls for this device."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = state.email ?: "Email unavailable",
                    style = MaterialTheme.typography.bodyLarge
                )
                HorizontalDivider()
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log out")
                }
            }
        }
    }
}

@Composable
private fun ReadingUnitRow(
    unit: ReadingUnit,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelected
        )
        Text(
            text = unit.label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
