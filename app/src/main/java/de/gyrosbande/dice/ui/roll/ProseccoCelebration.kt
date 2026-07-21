package de.gyrosbande.dice.ui.roll

import android.content.Context
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Text
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private const val DURATION_MS = 3000
private const val POP_AT = 0.42f // fraction of the timeline the cork pops at

private val ConfettiColors = listOf(
    Color(0xFFD4AF37), // gold
    Color(0xFFFF4E8A), // pink
    Color(0xFF4ECDC4), // teal
    Color(0xFF7B61FF), // purple
    Color(0xFF6BCB77), // green
    Color(0xFFFFD93D), // yellow
    Color(0xFFFFFFFF), // white
)

private class Confetto(
    val x: Float,
    val color: Color,
    val sizePx: Float,
    val delay: Float,
    val spin: Float,
    val wobble: Float,
    val wobbleAmp: Float,
)

private fun confettiBurst(count: Int): List<Confetto> = List(count) {
    Confetto(
        x = Random.nextFloat(),
        color = ConfettiColors[Random.nextInt(ConfettiColors.size)],
        sizePx = 16f + Random.nextFloat() * 22f,
        delay = Random.nextFloat() * 0.35f,
        spin = 1.5f + Random.nextFloat() * 3.5f,
        wobble = 4f + Random.nextFloat() * 6f,
        wobbleAmp = 0.01f + Random.nextFloat() * 0.04f,
    )
}

/**
 * A little party when the dreaded (beloved) bottle of Prosecco is rolled:
 * a bottle rides a rocket up, the cork pops, and confetti rains down.
 * Full-screen overlay, tap anywhere to skip, auto-dismisses when it ends.
 */
@Composable
fun ProseccoCelebration(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val progress = remember { Animatable(0f) }
        val confetti = remember { confettiBurst(90) }
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            progress.animateTo(1f, tween(DURATION_MS, easing = LinearEasing))
            onDismiss()
        }

        // Cork pop: a thump plus a burst of applause (both guarded, and the
        // applause honours the same mute toggle as the dice sound).
        LaunchedEffect(Unit) {
            delay((DURATION_MS * POP_AT).toLong())
            runCatching {
                @Suppress("DEPRECATION")
                context.getSystemService(Vibrator::class.java)
                    ?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            val soundOn = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getBoolean("soundEnabled", true)
            if (soundOn) runCatching {
                MediaPlayer.create(context, de.gyrosbande.dice.R.raw.prosecco_cheer)?.apply {
                    setOnCompletionListener { it.release() }
                    start()
                }
            }
        }

        val t = progress.value
        val popped = t >= POP_AT

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF20A0A0A))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Confetti (only after the pop).
            Canvas(Modifier.fillMaxSize()) {
                if (popped) confetti.forEach { drawConfetto(it, t) }
            }

            // Rocket + bottle, rising then hanging at the apex.
            val rise = FastOutSlowInEasing.transform((t / POP_AT).coerceIn(0f, 1f))
            BoxWithTransY(rise) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // A tiny bounce on the bottle at the moment of the pop.
                    val pop = if (popped) ((t - POP_AT) / (1f - POP_AT)).coerceIn(0f, 1f) else 0f
                    val bottleScale = 1f + 0.18f * sin(pop * Math.PI).toFloat()
                    Text(
                        "🍾",
                        fontSize = 88.sp,
                        modifier = Modifier.graphicsLayer(scaleX = bottleScale, scaleY = bottleScale),
                    )
                    // A flame trails the bottle while it blasts off.
                    if (!popped) {
                        Text("🔥", fontSize = 40.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            // Headline + caption.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (popped) {
                    Text("POP! 🎉", color = Color(0xFFD4AF37), fontSize = 40.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Flasche Prosecco!",
                        color = Color.White,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "Auf die Bande! 🥂",
                        color = Color(0xFFB5B5B5),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Offsets [content] vertically from below the centre up to the apex by [rise] (0..1). */
@Composable
private fun BoxWithTransY(rise: Float, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationY = lerp(size.height * 0.55f, -size.height * 0.06f, rise) },
        contentAlignment = Alignment.Center,
    ) { content() }
}

private fun DrawScope.drawConfetto(c: Confetto, t: Float) {
    val startT = POP_AT + c.delay * 0.2f
    val local = ((t - startT) / (1f - startT)).coerceIn(0f, 1f)
    if (local <= 0f) return
    val x = (c.x + sin(local * c.wobble) * c.wobbleAmp) * size.width
    val y = (-0.05f + local * 1.12f) * size.height
    val alpha = if (local < 0.85f) 1f else (1f - (local - 0.85f) / 0.15f)
    val rot = c.spin * local * 360f
    rotate(rot, pivot = Offset(x, y)) {
        drawRect(
            color = c.color.copy(alpha = alpha),
            topLeft = Offset(x - c.sizePx / 2f, y - c.sizePx / 2f),
            size = Size(c.sizePx, c.sizePx * 0.6f),
        )
    }
}
