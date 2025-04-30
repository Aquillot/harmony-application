package fr.harmony.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

// Data class pour les couleurs personnalisées
data class CustomColors(
    val darkCard: Color,
    val darkerCard: Color,
    val lightCard: Color,
    val lightCardStroke: Color,
    val darkCardStroke: Color,
    val darkerCardStroke: Color,
    val textColor: Color,
    val darkTextColor: Color,
    val subtleTextColor: Color,
    val disabledTextColor: Color,
    val errorColor: Color,
)

data class CustomTypography(
    val title: TextStyle,
    val smallLine: TextStyle,
)

// CompositionLocal pour injecter les couleurs
val LocalCustomColors = staticCompositionLocalOf<CustomColors> {
    error("No CustomColors provided")
}

val LocalCustomTypography = staticCompositionLocalOf<CustomTypography> {
    error("No CustomTypography provided")
}

// Thèmes Material
private val DarkColorScheme = darkColorScheme(
    primary = primaryColor,
    background = backgroundColor,
    surface = backgroundColor,
)

// AppTheme avec support clair/sombre et couleurs personnalisées
@Composable
fun HarmonyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

    val customColors = CustomColors(
        darkCard = darkCardBackgroundColor,
        darkerCard = darkerCardBackgroundColor,
        lightCard = lightCardBackgroundColor,
        darkCardStroke = darkCardStrokeColor,
        darkerCardStroke = darkerCardStrokeColor,
        lightCardStroke = lightCardStrokeColor,
        textColor = textColor,
        darkTextColor = darkTextColor,
        subtleTextColor = subtleTextColor,
        disabledTextColor = disabledTextColor,
        errorColor = errorColor
    )

    val customTypography = CustomTypography(
        title = titleLarge,
        smallLine = smallLine
    )

    CompositionLocalProvider(LocalCustomColors provides customColors, LocalCustomTypography provides customTypography) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

// Accès pratique
object AppTheme {
    val harmonyColors: CustomColors
        @Composable get() = LocalCustomColors.current
    val harmonyTypography: CustomTypography
        @Composable get() = LocalCustomTypography.current
}
