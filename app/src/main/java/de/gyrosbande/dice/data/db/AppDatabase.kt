package de.gyrosbande.dice.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CategoryEntity::class,
        DrinkEntity::class,
        PlayerEntity::class,
        RoundEntity::class,
        RollResultEntity::class,
        ExtraOrderItemEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun playerDao(): PlayerDao
    abstract fun roundDao(): RoundDao

    companion object {
        /**
         * v1 -> v2: rounds get a globally unique uuid (for cross-device
         * history merging); roll results record the category size at roll
         * time (for the wrap statistic; 0 = unknown for old rows).
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE rounds ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")
                // Backfill existing rounds with random ids.
                db.execSQL("UPDATE rounds SET uuid = lower(hex(randomblob(16)))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_rounds_uuid ON rounds(uuid)")
                db.execSQL("ALTER TABLE roll_results ADD COLUMN categorySize INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v2 -> v3: results track whether the drink was replaced by hand. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE roll_results ADD COLUMN substituted INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v3 -> v4: manually added order lines (food, beer ...) per round. */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `extra_order_items` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`roundId` INTEGER NOT NULL, " +
                        "`label` TEXT NOT NULL, " +
                        "`priceCents` INTEGER NOT NULL, " +
                        "`quantity` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`roundId`) REFERENCES `rounds`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_extra_order_items_roundId` " +
                        "ON `extra_order_items` (`roundId`)"
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dice837.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }
    }
}
