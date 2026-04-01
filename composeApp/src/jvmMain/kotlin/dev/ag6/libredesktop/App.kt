package dev.ag6.libredesktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import dev.ag6.libredesktop.ui.auth.AuthScreen
import dev.ag6.libredesktop.ui.theme.AppThemeController
import dev.ag6.libredesktop.ui.theme.LibreDesktopTheme
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val themeController = koinInject<AppThemeController>()
    val themeMode by themeController.themeMode.collectAsState()

    LibreDesktopTheme(themeMode = themeMode) {
        Navigator(screen = AuthScreen())
    }
}
