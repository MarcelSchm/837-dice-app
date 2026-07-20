package de.gyrosbande.dice.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class CategoryWithDrinks(
    @Embedded val category: CategoryEntity,
    @Relation(parentColumn = "id", entityColumn = "categoryId")
    val drinks: List<DrinkEntity>,
)

@Dao
interface MenuDao {
    @Transaction
    @Query("SELECT * FROM categories ORDER BY sortOrder")
    suspend fun categoriesWithDrinks(): List<CategoryWithDrinks>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun categoryCount(): Int

    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert
    suspend fun insertDrinks(drinks: List<DrinkEntity>)
}

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY id")
    fun observePlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players ORDER BY id")
    suspend fun players(): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE isActive = 1 ORDER BY id")
    suspend fun activePlayers(): List<PlayerEntity>

    @Insert
    suspend fun insert(player: PlayerEntity): Long

    @Update
    suspend fun update(player: PlayerEntity)

    @Delete
    suspend fun delete(player: PlayerEntity)
}

data class RoundWithResults(
    @Embedded val round: RoundEntity,
    @Relation(parentColumn = "id", entityColumn = "roundId")
    val results: List<RollResultEntity>,
)

@Dao
interface RoundDao {
    @Insert
    suspend fun insertRound(round: RoundEntity): Long

    @Insert
    suspend fun insertResult(result: RollResultEntity)

    @Insert
    suspend fun insertResults(results: List<RollResultEntity>)

    @Query("UPDATE rounds SET finishedAt = :finishedAt WHERE id = :roundId")
    suspend fun finishRound(roundId: Long, finishedAt: Long)

    /** Only finished rounds count for history, statistics and export. */
    @Transaction
    @Query("SELECT * FROM rounds WHERE finishedAt IS NOT NULL ORDER BY startedAt DESC")
    fun observeFinishedRounds(): Flow<List<RoundWithResults>>

    @Transaction
    @Query("SELECT * FROM rounds WHERE finishedAt IS NOT NULL ORDER BY startedAt DESC")
    suspend fun finishedRounds(): List<RoundWithResults>

    @Query("SELECT uuid FROM rounds")
    suspend fun allRoundUuids(): List<String>
}
