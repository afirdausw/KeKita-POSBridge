package devyana.kekita.posbridge.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import devyana.kekita.posbridge.data.local.dao.ProductDao
import devyana.kekita.posbridge.data.local.entity.ProductEntity
import devyana.kekita.posbridge.data.local.entity.SyncStatus

// ─── Type Converters ───────────────────────────────────────────────────────────

class AppTypeConverters {

    @TypeConverter
    fun fromSyncStatus(syncStatus: SyncStatus): String {
        return syncStatus.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }
}

// ─── Migrations ────────────────────────────────────────────────────────────────

/**
 * Migration from version 1 to 2.
 * Add new migrations here as the schema evolves.
 * Never drop columns that contain unsynced data.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Example: add a new column without breaking existing data
        // db.execSQL("ALTER TABLE products ADD COLUMN barcode TEXT NOT NULL DEFAULT ''")
    }
}

// ─── AppDatabase ───────────────────────────────────────────────────────────────

@Database(
    entities = [
        ProductEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {

        private const val DATABASE_NAME = "kekita_posbridge_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }
    }
}
