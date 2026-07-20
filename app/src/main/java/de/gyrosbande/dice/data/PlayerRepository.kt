package de.gyrosbande.dice.data

import de.gyrosbande.dice.data.db.PlayerDao
import de.gyrosbande.dice.data.db.PlayerEntity
import de.gyrosbande.dice.domain.Player
import de.gyrosbande.dice.domain.PlayerName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerRepository(private val playerDao: PlayerDao) {

    fun observePlayers(): Flow<List<Player>> =
        playerDao.observePlayers().map { entities -> entities.map { it.toDomain() } }

    suspend fun activePlayers(): List<Player> =
        playerDao.activePlayers().map { it.toDomain() }

    /**
     * Adds a player. Names are capitalized and must be unique (ignoring
     * case) - with two Marcels in the group, one becomes "Marcel S", so
     * every name in the order summary points at exactly one person.
     * Returns false when the name was blank or already taken.
     */
    suspend fun add(name: String): Boolean {
        if (PlayerName.isBlank(name)) return false
        if (PlayerName.isTaken(name, playerDao.players().map { it.name })) return false
        playerDao.insert(PlayerEntity(name = PlayerName.normalize(name)))
        return true
    }

    suspend fun setActive(player: Player, active: Boolean) =
        playerDao.update(PlayerEntity(player.id, player.name, active))

    suspend fun remove(player: Player) =
        playerDao.delete(PlayerEntity(player.id, player.name, player.isActive))

    private fun PlayerEntity.toDomain() = Player(id, name, isActive)
}
