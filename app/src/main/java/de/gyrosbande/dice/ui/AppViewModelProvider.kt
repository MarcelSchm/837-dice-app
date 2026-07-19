package de.gyrosbande.dice.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.gyrosbande.dice.DiceApp
import de.gyrosbande.dice.ui.players.PlayersViewModel
import de.gyrosbande.dice.ui.roll.QuickRollViewModel
import de.gyrosbande.dice.ui.round.RoundViewModel

/** Factory wiring ViewModels to the repositories in [DiceApp]. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            QuickRollViewModel(diceApp().menuRepository)
        }
        initializer {
            PlayersViewModel(diceApp().playerRepository)
        }
        initializer {
            RoundViewModel(
                menuRepository = diceApp().menuRepository,
                playerRepository = diceApp().playerRepository,
                roundRepository = diceApp().roundRepository,
            )
        }
    }
}

private fun CreationExtras.diceApp(): DiceApp =
    this[AndroidViewModelFactory.APPLICATION_KEY] as DiceApp
