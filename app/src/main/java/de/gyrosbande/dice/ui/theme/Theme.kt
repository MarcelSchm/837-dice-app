package de.gyrosbande.dice.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Color world based on the logo: black/white with a gold accent.
val Gold = Color(0xFFD4AF37)
val GoldDark = Color(0xFFA8862A)
val NearBlack = Color(0xFF111111)
val CardGrey = Color(0xFF1E1E1E)

private val GyrosbandeColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color.Black,
    secondary = GoldDark,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = NearBlack,
    onSurface = Color.White,
    surfaceVariant = CardGrey,
    onSurfaceVariant = Color(0xFFCCCCCC),
)

/** The app is always dark - like the festival night. */
@Composable
fun Dice837Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GyrosbandeColors,
        content = content,
    )
}
