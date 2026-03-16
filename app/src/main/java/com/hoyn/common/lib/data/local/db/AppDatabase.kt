package com.hoyn.common.lib.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hoyn.common.lib.data.local.dao.PostDao
import com.hoyn.common.lib.data.local.entity.PostEntity

@Database(
    entities = [PostEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao

    companion object {
        private const val DATABASE_NAME = "common_lib_demo.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { database ->
                    INSTANCE = database
                }
            }
        }
    }
}