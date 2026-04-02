package com.hoyn.common.lib.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hoyn.common.lib.data.local.dao.PostDao
import com.hoyn.common.lib.data.local.entity.PostEntity

/**
 * 应用数据库
 *
 * Room 数据库配置类，定义数据库版本、实体和访问对象
 * 使用双重检查锁定（DCL）保证单例线程安全
 */
@Database(
    entities = [PostEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * 获取帖子数据访问对象
     *
     * @return PostDao 实例
     */
    abstract fun postDao(): PostDao

    companion object {
        /** 数据库文件名 */
        private const val DATABASE_NAME = "common_lib_demo.db"

        /** 数据库单例实例，使用 @Volatile 保证多线程可见性 */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库单例实例
         *
         * 使用双重检查锁定（Double-Checked Locking）保证线程安全
         *
         * @param context Context 实例
         * @return AppDatabase 数据库实例
         */
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