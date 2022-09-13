package com.example.fusedlocationapi

import android.content.Context
import android.os.Environment
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Location::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        private var instance: AppDatabase? = null
        val DB_NAME = "/location_db"
        val DB_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path

        @Synchronized
        fun getInstance(ctx: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    ctx, AppDatabase::class.java, "$DB_PATH$DB_NAME"
                ).setJournalMode(JournalMode.TRUNCATE)
                    //.fallbackToDestructiveMigration()
                    .build()
            }
            return instance!!
        }
    }
}