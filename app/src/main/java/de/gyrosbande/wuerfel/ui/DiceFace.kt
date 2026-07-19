package de.gyrosbande.wuerfel.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Klassisches Würfel-Auge-Layout pro Augenzahl (relative Positionen). */
private val pipLayouts: Map<Int, List<Pair<Float, Float>>> = mapOf(
    1 to listOf(0.5f to 0.5f),
    2 to listOf(0.25f to 0.25f, 0.75f to 0.75f),
    3 to listOf(0.25f to 0.25f, 0.5f to 0.5f, 0.75f to 0.75f),
    4 to listOf(0.25f to 0.25f, 0.75f to 0.25f, 0.25f to 0.75f, 0.75f to 0.75f),
    5 to listOf(0.25f to 0.25f, 0.75f to 0.25f, 0.5f to 0.5f, 0.25f to 0.75f, 0.75f to 0.75f),
    6 to listOf(
        0.25f to 0.25f, 0.75f to 0.25f,
        0.25f to 0.5f, 0.75f to 0.5f,
        0.25f to 0.75f, 0.75f to 0.75f,
    ),
)

/** Ein Würfel mit Augen; [value] null zeigt einen leeren Platzhalter. */
@Composable
fun DiceFace(value: Int?, size: Dp = 96.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = if (value == null) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                shape = RoundedCornerShape(size / 6),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (value == null) {
            Text("?", style = MaterialTheme.typography.headlineLarge)
        } else {
            Canvas(modifier = Modifier.size(size).padding(size / 12)) {
                val pips = pipLayouts.getValue(value.coerceIn(1, 6))
                val radius = this.size.minDimension / 12
                pips.forEach { (x, y) ->
                    drawCircle(
                        color = Color.Black,
                        radius = radius,
                        center = Offset(this.size.width * x, this.size.height * y),
                    )
                }
            }
        }
    }
}
