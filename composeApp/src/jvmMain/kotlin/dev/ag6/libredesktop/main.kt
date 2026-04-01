package dev.ag6.libredesktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.ag6.libredesktop.di.initKoin

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "LibreDesktop",
    ) {
        App()
    }
}

