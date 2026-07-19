package de.gyrosbande.wuerfel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Farbwelt am Logo orientiert: Schwarz/Weiß mit Gold-Akzent.
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

/** Die App ist immer dunkel – wie die Festival-Nacht. */
@Composable
fun Wuerfel837Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GyrosbandeColors,
        content = content,
    )
}
