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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import de.gyrosbande.dice.domain.Category
import de.gyrosbande.dice.domain.GameFlow
import de.gyrosbande.dice.domain.MenuSeed
import de.gyrosbande.dice.domain.RollPhase
import de.gyrosbande.dice.domain.sync.RoundStage
import de.gyrosbande.dice.domain.sync.WatchRoundState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val Gold = Color(0xFFD4AF37)
private val Grey = Color(0xFFB5B5B5)

class MainActivity : ComponentActivity() {

    private lateinit var watchMenu: WatchMenu
    private lateinit var watchRound: WatchRound

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        watchMenu = WatchMenu(this)
        watchRound = WatchRound(this)
        setContent { WatchApp(watchMenu, watchRound) }
    }

    override fun onResume() {
        super.onResume()
        watchMenu.start()
        watchRound.start()
    }

    override fun onPause() {
        super.onPause()
        watchMenu.stop()
        watchRound.stop()
    }
}

/**
 * Entry point: when the phone is running a connected round the watch turns
 * into the dice cup for it (phase 2b); otherwise it is the standalone
 * quick-roll (phase 1) on the phone's synced menu (phase 2a).
 */
@Composable
fun WatchApp(watchMenu: WatchMenu, watchRound: WatchRound) {
    val round by watchRound.state.collectAsState()
    if (round.active) {
        ConnectedRoundScreen(round)
    } else {
        QuickRollScreen(watchMenu)
    }
}

/**
 * The standalone quick-roll: tap (or shake the wrist) to roll, first the
 * category, then the drink. Uses the shared :core game logic - same rules
 * as the phone. Rolls on the menu the phone synced ([watchMenu]); falls
 * back to the bundled seed when no phone has synced yet, so it still works
 * fully standalone.
 */
@Composable
private fun QuickRollScreen(watchMenu: WatchMenu) {
    val syncedMenu by watchMenu.categories.collectAsState()
    val categories: List<Category> = syncedMenu ?: MenuSeed.categories

    var flow by remember { mutableStateOf(GameFlow(categories)) }
    var phase by remember { mutableStateOf(flow.phase) }
    var shownDice by remember { mutableStateOf(emptyList<Int>()) }
    var rolling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun vibrate(milliseconds: Long) {
        context.getSystemService(Vibrator::class.java)
            ?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    // Adopt a freshly synced menu, but only while sitting idle at the start
    // so a phone edit never rewrites the card mid-round.
    LaunchedEffect(categories) {
        if (flow.phase is RollPhase.CategoryRoll && !rolling) {
            flow = GameFlow(categories)
            phase = flow.phase
        }
    }

    fun rollOrRestart() {
        if (rolling) return
        if (flow.phase is RollPhase.Finished) {
            // Start the next round on the latest synced menu.
            flow = GameFlow(categories)
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
                is RollPhase.CategoryRoll -> StartContent(shownDice, rolling, synced = syncedMenu != null)
                is RollPhase.DrinkRoll -> CategoryContent(p, shownDice, rolling)
                is RollPhase.Finished -> ResultContent(p)
            }
        }
    }
}

/**
 * The watch as a live second display for a round running on the phone
 * (phase 2b). Purely passive - it mirrors whose turn it is and the rolled
 * drink, but never rolls: all rolling happens in the phone app, so the
 * watch can be jostled without any effect.
 */
@Composable
private fun ConnectedRoundScreen(round: WatchRoundState) {
    val context = LocalContext.current

    // Buzz when a fresh result comes in.
    LaunchedEffect(round.stage, round.playerIndex) {
        if (round.stage == RoundStage.RESULT) {
            context.getSystemService(Vibrator::class.java)
                ?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(18.dp),
            ) {
                if (round.stage == RoundStage.DONE) {
                    Text("Runde fertig 🍻", color = Gold, fontSize = 18.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text("Weiter geht's am Handy", color = Grey, fontSize = 11.sp, textAlign = TextAlign.Center)
                    return@Column
                }

                Text(
                    "Spieler ${round.playerIndex + 1}/${round.totalPlayers}",
                    color = Grey,
                    fontSize = 11.sp,
                )
                Text(
                    round.currentPlayer ?: "",
                    color = Gold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))

                when {
                    round.rolling -> Text("🎲 …", color = Color.White, fontSize = 30.sp)
                    round.stage == RoundStage.CATEGORY ->
                        RoundHint("ist dran", "würfelt am Handy")
                    round.stage == RoundStage.DRINK ->
                        RoundHint(round.category ?: "", "Drink kommt gleich")
                    round.stage == RoundStage.RESULT -> ResultLine(round)
                }
            }
        }
    }
}

@Composable
private fun RoundHint(top: String, action: String) {
    Text(top, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(6.dp))
    Text(action, color = Grey, fontSize = 12.sp, textAlign = TextAlign.Center)
}

@Composable
private fun ResultLine(round: WatchRoundState) {
    Text(round.resultDrink ?: "", color = Gold, fontSize = 18.sp, textAlign = TextAlign.Center)
    round.resultPrice?.let {
        Spacer(Modifier.height(2.dp))
        Text(it, color = Color.White, fontSize = 15.sp)
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
private fun StartContent(dice: List<Int>, rolling: Boolean, synced: Boolean) {
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
        Spacer(Modifier.height(4.dp))
        Text(
            if (synced) "🔗 Karte vom Handy" else "Standardkarte",
            color = Grey,
            fontSize = 10.sp,
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
