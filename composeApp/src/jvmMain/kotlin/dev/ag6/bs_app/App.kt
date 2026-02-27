package dev.ag6.bs_app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import dev.ag6.bs_app.screen.auth.AuthScreen

@Composable
@Preview
fun App() {
    Navigator(screen = AuthScreen())
}