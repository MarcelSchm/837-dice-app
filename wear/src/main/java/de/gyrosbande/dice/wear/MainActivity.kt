package de.gyrosbande.dice.wear

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import de.gyrosbande.dice.domain.GameFlow
import de.gyrosbande.dice.domain.RollPhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val Gold = Color(0xFFD4AF37)
private val Grey = Color(0xFFB5B5B5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WatchApp() }
    }
}

/**
 * The whole phase-1 watch app on one screen: tap (or shake the wrist)
 * to roll, first the category, then the drink. Uses the shared :core
 * game logic - same rules as the phone, standalone (no phone needed).
 */
@Composable
fun WatchApp() {
    val flow = remember { GameFlow() }
    var phase by remember { mutableStateOf(flow.phase) }
    var shownDice by remember { mutableStateOf(emptyList<Int>()) }
    var rolling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun vibrate(milliseconds: Long) {
        context.getSystemService(Vibrator::class.java)
            ?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun rollOrRestart() {
        if (rolling) return
        if (flow.phase is RollPhase.Finished) {
            flow.reset()
            phase = flow.phase
            shownDice = emptyList()
            return
        }
        scope.launch {
            rolling = true
            val diceCount = flow.requiredDice()
            repeat(8) {
                shownDice = List(diceCount) { Random.nextInt(1, 7) }
                delay(70)
            }
            shownDice = flow.rollVirtual()
            phase = flow.phase
            rolling = false
            // The result lands with a thump - haptics instead of sound.
            vibrate(if (flow.phase is RollPhase.Finished) 120 else 50)
        }
    }

    WatchShake(enabled = !rolling && phase !is RollPhase.Finished) { rollOrRestart() }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = ::rollOrRestart,
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (val p = phase) {
                is RollPhase.CategoryRoll -> StartContent(shownDice, rolling)
                is RollPhase.DrinkRoll -> CategoryContent(p, shownDice, rolling)
                is RollPhase.Finished -> ResultContent(p)
            }
        }
    }
}

@Composable
private fun DiceLine(dice: List<Int>, placeholderCount: Int) {
    val text = if (dice.isEmpty()) {
        List(placeholderCount) { "?" }.joinToString("  ")
    } else {
        dice.joinToString("  ")
    }
    Text(text, fontSize = 32.sp, color = Color.White, textAlign = TextAlign.Center)
}

@Composable
private fun StartContent(dice: List<Int>, rolling: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text("837 Dice", color = Gold, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        Text("Wurf 1: Kategorie", color = Color.White, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        if (rolling) DiceLine(dice, 1) else Text("🎲", fontSize = 34.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Tippen oder Handgelenk schütteln",
            color = Grey,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CategoryContent(phase: RollPhase.DrinkRoll, dice: List<Int>, rolling: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text("Wurf 2", color = Grey, fontSize = 11.sp)
        Text(
            phase.category.name,
            color = Gold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (phase.diceCount == 2) "${phase.category.drinks.size} Drinks, zwei Würfel"
            else "${phase.category.drinks.size} Drinks, ein Würfel",
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        DiceLine(if (rolling) dice else emptyList(), phase.diceCount)
        Spacer(Modifier.height(8.dp))
        Text("Nochmal tippen oder schütteln", color = Grey, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ResultContent(phase: RollPhase.Finished) {
    val outcome = phase.outcome
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
        Text("Das wird bestellt! 🍻", color = Color.White, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            outcome.drink.name,
            color = Gold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
        outcome.drink.sizeLabel?.let {
            Text(it, color = Grey, fontSize = 11.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(outcome.drink.priceFormatted, color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "${outcome.category.name} · ${outcome.categoryRoll} + ${outcome.drinkRolls.joinToString("+")}",
            color = Grey,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text("Tippen für neue Runde", color = Grey, fontSize = 10.sp)
    }
}
