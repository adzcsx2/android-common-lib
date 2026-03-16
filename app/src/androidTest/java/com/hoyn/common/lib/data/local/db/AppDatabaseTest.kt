package com.hoyn.common.lib.data.local.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hoyn.common.lib.data.local.dao.PostDao
import com.hoyn.common.lib.data.local.entity.PostEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AppDatabase 仪器测试
 *
 * 测试数据库创建和 DAO 获取
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var postDao: PostDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        postDao = database.postDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testDatabaseCreation() {
        assertEquals(true, database.isOpen)
    }

    @Test
    fun testPostDaoRetrieval() {
        val dao = database.postDao()
        assertEquals(true, dao is PostDao)
    }
}
