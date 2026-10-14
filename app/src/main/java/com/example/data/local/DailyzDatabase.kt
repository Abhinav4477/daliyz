package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WorkSession::class, WorkNote::class, NoteTodoItem::class],
    version = 1,
    exportSchema = false
)
abstract class DailyzDatabase : RoomDatabase() {

    abstract fun workSessionDao(): WorkSessionDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: DailyzDatabase? = null

        fun getDatabase(context: Context): DailyzDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DailyzDatabase::class.java,
                    "dailyz_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
