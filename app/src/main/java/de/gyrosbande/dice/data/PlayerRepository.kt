package de.gyrosbande.dice.data

import de.gyrosbande.dice.data.db.PlayerDao
import de.gyrosbande.dice.data.db.PlayerEntity
import de.gyrosbande.dice.domain.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerRepository(private val playerDao: PlayerDao) {

    fun observePlayers(): Flow<List<Player>> =
        playerDao.observePlayers().map { entities -> entities.map { it.toDomain() } }

    suspend fun activePlayers(): List<Player> =
        playerDao.activePlayers().map { it.toDomain() }

    suspend fun add(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) playerDao.insert(PlayerEntity(name = trimmed))
    }

    suspend fun setActive(player: Player, active: Boolean) =
        playerDao.update(PlayerEntity(player.id, player.name, active))

    suspend fun remove(player: Player) =
        playerDao.delete(PlayerEntity(player.id, player.name, player.isActive))

    private fun PlayerEntity.toDomain() = Player(id, name, isActive)
}
