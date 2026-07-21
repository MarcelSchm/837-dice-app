package de.gyrosbande.dice

import android.app.Application
import de.gyrosbande.dice.data.HistoryRepository
import de.gyrosbande.dice.data.MenuRepository
import de.gyrosbande.dice.data.PlayerRepository
import de.gyrosbande.dice.data.RoundRepository
import de.gyrosbande.dice.data.SettingsRepository
import de.gyrosbande.dice.data.db.AppDatabase
import de.gyrosbande.dice.data.sync.MenuSyncPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency container - deliberately no DI framework for an app
 * this size (see CLAUDE.md).
 */
class DiceApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val menuRepository: MenuRepository by lazy { MenuRepository(database) }
    val playerRepository: PlayerRepository by lazy { PlayerRepository(database.playerDao()) }
    val roundRepository: RoundRepository by lazy { RoundRepository(database.roundDao()) }
    val historyRepository: HistoryRepository by lazy { HistoryRepository(database) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    /** Lives as long as the process - for background work not tied to a screen. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val menuSyncPublisher by lazy { MenuSyncPublisher(this, menuRepository) }

    override fun onCreate() {
        super.onCreate()
        // Mirror the menu to any paired watch (best-effort, see WEAR.md).
        menuSyncPublisher.start(appScope)
    }
}
