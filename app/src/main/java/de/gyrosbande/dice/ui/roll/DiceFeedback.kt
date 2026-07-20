package de.gyrosbande.dice.ui.roll

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

/** Plays the dice rattle (unless muted; the choice is persisted). */
class DiceFeedback(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private var loaded = false
    private var currentStream = 0

    private val soundId = soundPool
        .also { pool ->
            // Loading is asynchronous - without this the very first roll
            // right after app start would stay silent.
            pool.setOnLoadCompleteListener { _, _, status -> loaded = status == 0 }
        }
        .load(context, de.gyrosbande.dice.R.raw.dice_roll, 1)

    var soundEnabled by mutableStateOf(prefs.getBoolean("soundEnabled", true))
        private set

    fun toggleSound() {
        soundEnabled = !soundEnabled
        prefs.edit().putBoolean("soundEnabled", soundEnabled).apply()
        if (!soundEnabled) stop()
    }

    fun playRoll() {
        if (!soundEnabled || !loaded) return
        // One roll at a time - a second roll restarts the build-up.
        stop()
        currentStream = soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    /** Cuts the sound short, e.g. when leaving the screen mid-roll. */
    fun stop() {
        if (currentStream != 0) {
            soundPool.stop(currentStream)
            currentStream = 0
        }
    }

    fun release() = soundPool.release()
}

@Composable
fun rememberDiceFeedback(): DiceFeedback {
    val context = LocalContext.current
    val feedback = remember { DiceFeedback(context.applicationContext) }
    DisposableEffect(Unit) {
        onDispose {
            feedback.stop()
            feedback.release()
        }
    }
    return feedback
}

/**
 * Triggers [onShake] when the phone is shaken (accelerometer spike above
 * ~2.3g, debounced) - shaking the phone rolls the dice, like a dice cup.
 */
@Composable
fun ShakeToRoll(enabled: Boolean, onShake: () -> Unit) {
    val context = LocalContext.current
    val currentEnabled by rememberUpdatedState(enabled)
    val currentOnShake by rememberUpdatedState(onShake)

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastTrigger = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!currentEnabled) return
                val (x, y, z) = event.values
                val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                val now = System.currentTimeMillis()
                if (gForce > 2.3f && now - lastTrigger > 1_200) {
                    lastTrigger = now
                    currentOnShake()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }
}
