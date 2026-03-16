package dev.ag6.libredesktop.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import dev.ag6.libredesktop.model.reading.ReadingUnit
import dev.ag6.libredesktop.ui.auth.AuthScreen

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SettingsScreenModel>()
        val state by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.current

        SettingsScreenContent(
            state = state,
            onBack = { navigator?.pop() },
            onReadingUnitSelected = screenModel::onReadingUnitSelected,
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
    onReadingUnitSelected: (ReadingUnit) -> Unit,
    onLogout: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium
            )

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccountSection(
                        email = state.email,
                        onLogout = onLogout
                    )
                    HorizontalDivider()
                    Text(
                        text = "Glucose units",
                        style = MaterialTheme.typography.titleMedium
                    )
                    ReadingUnit.entries.forEach { unit ->
                        ReadingUnitRow(
                            unit = unit,
                            selected = state.readingUnit == unit,
                            onSelected = { onReadingUnitSelected(unit) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun AccountSection(
    email: String?,
    onLogout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = email ?: "Email unavailable",
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
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
            .padding(vertical = 4.dp),
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
