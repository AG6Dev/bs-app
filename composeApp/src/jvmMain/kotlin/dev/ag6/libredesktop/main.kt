package dev.ag6.libredesktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import dev.ag6.libredesktop.di.initKoin

fun main() {
    initKoin()
    application {
        NotifierManager.initialize(
            NotificationPlatformConfiguration.Desktop()
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = "LibreDesktop",
        ) {
            App()
        }
    }
}
