package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.domain.DayStatus
import com.example.domain.StreakGoalType
import com.example.domain.TaskPriority

class TileTrackConverters {
    @TypeConverter
    fun fromStreakGoalType(value: StreakGoalType): String = value.name

    @TypeConverter
    fun toStreakGoalType(value: String): StreakGoalType =
        runCatching { StreakGoalType.valueOf(value) }.getOrDefault(StreakGoalType.DAILY)

    @TypeConverter
    fun fromDayStatus(value: DayStatus): String = value.name

    @TypeConverter
    fun toDayStatus(value: String): DayStatus =
        runCatching { DayStatus.valueOf(value) }.getOrDefault(DayStatus.UNDONE)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority =
        runCatching { TaskPriority.valueOf(value) }.getOrDefault(TaskPriority.MEDIUM)
}

@Database(
    entities = [
        HabitEntity::class,
        CompletionLogEntity::class,
        ReminderEntity::class,
        TaskEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(TileTrackConverters::class)
abstract class TileTrackDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var instance: TileTrackDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE completion_logs ADD COLUMN status TEXT NOT NULL DEFAULT 'DONE'")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tasks (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        dueEpochDay INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        colorArgb INTEGER NOT NULL,
                        dueTimeMinutes INTEGER,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        isDeleted INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_dueEpochDay ON tasks(dueEpochDay)")
            }
        }

        fun getInstance(context: Context): TileTrackDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TileTrackDatabase::class.java,
                    "tiletrack.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
