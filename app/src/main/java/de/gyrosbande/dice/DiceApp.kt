package de.gyrosbande.dice

import android.app.Application
import de.gyrosbande.dice.data.MenuRepository
import de.gyrosbande.dice.data.PlayerRepository
import de.gyrosbande.dice.data.RoundRepository
import de.gyrosbande.dice.data.db.AppDatabase

/**
 * Manual dependency container - deliberately no DI framework for an app
 * this size (see CLAUDE.md).
 */
class DiceApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val menuRepository: MenuRepository by lazy { MenuRepository(database.menuDao()) }
    val playerRepository: PlayerRepository by lazy { PlayerRepository(database.playerDao()) }
    val roundRepository: RoundRepository by lazy { RoundRepository(database.roundDao()) }
}
