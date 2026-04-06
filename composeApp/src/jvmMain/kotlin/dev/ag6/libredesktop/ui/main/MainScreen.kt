package dev.ag6.libredesktop.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import dev.ag6.libredesktop.ui.alarms.AlarmsScreen
import dev.ag6.libredesktop.ui.components.SidebarNavigation
import dev.ag6.libredesktop.ui.overview.OverviewScreen
import dev.ag6.libredesktop.ui.settings.SettingsScreen

class MainScreen : Screen {
    @Composable
    override fun Content() {
        TabNavigator(OverviewScreen) {
            val tabNavigator = LocalTabNavigator.current
            val tabs = listOf(OverviewScreen, AlarmsScreen, SettingsScreen)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                SidebarNavigation(
                    tabs = tabs,
                    currentTab = tabNavigator.current,
                    onTabSelected = { tabNavigator.current = it }
                )
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    CurrentTab()
                }
            }
        }
    }
}