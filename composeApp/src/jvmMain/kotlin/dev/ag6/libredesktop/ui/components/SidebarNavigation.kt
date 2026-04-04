package dev.ag6.libredesktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab

@Composable
fun SidebarNavigation(
    tabs: List<Tab>,
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxHeight().width(88.dp),
        color = colors.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.10f),
                            colors.surface,
                            colors.secondary.copy(alpha = 0.06f)
                        )
                    )
                )
                .padding(vertical = 24.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LD",
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = colors.outlineVariant)
            Spacer(Modifier.height(16.dp))

            tabs.forEach { tab ->
                val isSelected = tab == currentTab
                SidebarNavItem(
                    tab = tab,
                    isSelected = isSelected,
                    onClick = { onTabSelected(tab) }
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun SidebarNavItem(
    tab: Tab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (isSelected) colors.primary.copy(alpha = 0.14f) else colors.surface.copy(alpha = 0f)
    val contentColor = if (isSelected) colors.primary else colors.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tab.options.icon?.let { painter ->
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = contentColor
            )
        }
        Text(
            text = tab.options.title,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
