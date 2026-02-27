package dev.ag6.bs_app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.ag6.bs_app.di.initKoin

fun main() = application {
    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "bs_app",
    ) {
        App()
    }
}