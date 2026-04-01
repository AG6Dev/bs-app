package dev.ag6.libredesktop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ag6.libredesktop.model.theme.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF0D6E6E),
    onPrimary = Color(0xFFF5FEFC),
    primaryContainer = Color(0xFFB4ECE7),
    onPrimaryContainer = Color(0xFF00201F),
    secondary = Color(0xFF5C5F8F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3E0FF),
    onSecondaryContainer = Color(0xFF1B1D4A),
    tertiary = Color(0xFF6D5E0F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF8E287),
    onTertiaryContainer = Color(0xFF221B00),
    error = Color(0xFFB42318),
    onError = Color.White,
    errorContainer = Color(0xFFFDE7E4),
    onErrorContainer = Color(0xFF4A0F0B),
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFDFEFF),
    onSurface = Color(0xFF101828),
    surfaceVariant = Color(0xFFE8EDF4),
    onSurfaceVariant = Color(0xFF445164),
    outline = Color(0xFF758195),
    outlineVariant = Color(0xFFCAD3DF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7ED8CF),
    onPrimary = Color(0xFF003735),
    primaryContainer = Color(0xFF004F4C),
    onPrimaryContainer = Color(0xFFA6F2EA),
    secondary = Color(0xFFC4C2FF),
    onSecondary = Color(0xFF2B2E5E),
    secondaryContainer = Color(0xFF434575),
    onSecondaryContainer = Color(0xFFE2E0FF),
    tertiary = Color(0xFFE2C86C),
    onTertiary = Color(0xFF3B2F00),
    tertiaryContainer = Color(0xFF544500),
    onTertiaryContainer = Color(0xFFFFE18B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF091118),
    onBackground = Color(0xFFE5EDF6),
    surface = Color(0xFF0F1722),
    onSurface = Color(0xFFE5EDF6),
    surfaceVariant = Color(0xFF1A2532),
    onSurfaceVariant = Color(0xFFB9C5D3),
    outline = Color(0xFF8793A2),
    outlineVariant = Color(0xFF344150),
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 54.sp, lineHeight = 58.sp, fontWeight = FontWeight.SemiBold),
    displayMedium = TextStyle(fontSize = 42.sp, lineHeight = 46.sp, fontWeight = FontWeight.SemiBold),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

@Composable
fun LibreDesktopTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}