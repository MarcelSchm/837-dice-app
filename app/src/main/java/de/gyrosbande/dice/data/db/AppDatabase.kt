package de.gyrosbande.dice.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CategoryEntity::class,
        DrinkEntity::class,
        PlayerEntity::class,
        RoundEntity::class,
        RollResultEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun playerDao(): PlayerDao
    abstract fun roundDao(): RoundDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dice837.db",
                ).build().also { instance = it }
            }
    }
}
