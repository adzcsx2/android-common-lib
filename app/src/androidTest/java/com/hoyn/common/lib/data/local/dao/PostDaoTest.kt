package com.hoyn.common.lib.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hoyn.common.lib.data.local.db.AppDatabase
import com.hoyn.common.lib.data.local.entity.PostEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PostDao 仪器测试
 *
 * 使用内存数据库测试 Room DAO 操作
 */
@RunWith(AndroidJUnit4::class)
class PostDaoTest {

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
    fun testInsertPosts() = runBlocking {
        val posts = listOf(
            PostEntity(userId = 1, id = 1, title = "Test 1", body = "Body 1", cachedAt = System.currentTimeMillis()),
            PostEntity(userId = 1, id = 2, title = "Test 2", body = "Body 2", cachedAt = System.currentTimeMillis())
        )

        postDao.insertPosts(posts)

        val result = postDao.getAllPosts()
        assertEquals(2, result.size)
    }

    @Test
    fun testGetAllPosts() = runBlocking {
        // Initially empty
        var result = postDao.getAllPosts()
        assertTrue(result.isEmpty())

        // Insert posts
        val posts = listOf(
            PostEntity(userId = 1, id = 1, title = "Test", body = "Body", cachedAt = 1000L)
        )
        postDao.insertPosts(posts)

        result = postDao.getAllPosts()
        assertEquals(1, result.size)
        assertEquals("Test", result[0].title)
    }

    @Test
    fun testClearPosts() = runBlocking {
        // Insert posts
        val posts = listOf(
            PostEntity(userId = 1, id = 1, title = "Test", body = "Body", cachedAt = 1000L)
        )
        postDao.insertPosts(posts)
        assertEquals(1, postDao.getAllPosts().size)

        // Clear posts
        postDao.clearPosts()
        assertTrue(postDao.getAllPosts().isEmpty())
    }

    @Test
    fun testGetCount() = runBlocking {
        // Initially 0
        assertEquals(0, postDao.getCount())

        // Insert posts
        val posts = listOf(
            PostEntity(userId = 1, id = 1, title = "Test 1", body = "Body 1", cachedAt = 1000L),
            PostEntity(userId = 1, id = 2, title = "Test 2", body = "Body 2", cachedAt = 1000L),
            PostEntity(userId = 1, id = 3, title = "Test 3", body = "Body 3", cachedAt = 1000L)
        )
        postDao.insertPosts(posts)

        assertEquals(3, postDao.getCount())
    }

    @Test
    fun testGetLatestCacheTime() = runBlocking {
        // Initially null
        assertNull(postDao.getLatestCacheTime())

        // Insert posts with different cache times
        val posts = listOf(
            PostEntity(userId = 1, id = 1, title = "Test 1", body = "Body 1", cachedAt = 1000L),
            PostEntity(userId = 1, id = 2, title = "Test 2", body = "Body 2", cachedAt = 3000L),
            PostEntity(userId = 1, id = 3, title = "Test 3", body = "Body 3", cachedAt = 2000L)
        )
        postDao.insertPosts(posts)

        // Should return the maximum cache time
        assertEquals(3000L, postDao.getLatestCacheTime())
    }

    @Test
    fun testInsertPostsReplace() = runBlocking {
        // Insert initial post
        val initialPost = PostEntity(userId = 1, id = 1, title = "Original", body = "Original Body", cachedAt = 1000L)
        postDao.insertPosts(listOf(initialPost))

        // Insert post with same id (should replace)
        val updatedPost = PostEntity(userId = 1, id = 1, title = "Updated", body = "Updated Body", cachedAt = 2000L)
        postDao.insertPosts(listOf(updatedPost))

        val result = postDao.getAllPosts()
        assertEquals(1, result.size)
        assertEquals("Updated", result[0].title)
        assertEquals("Updated Body", result[0].body)
    }

    @Test
    fun testInsertEmptyList() = runBlocking {
        postDao.insertPosts(emptyList())
        assertEquals(0, postDao.getCount())
    }

    @Test
    fun testInsertLargeDataSet() = runBlocking {
        val largePosts = (1..100).map { id ->
            PostEntity(
                userId = id % 10,
                id = id,
                title = "Title $id",
                body = "Body $id",
                cachedAt = System.currentTimeMillis()
            )
        }

        postDao.insertPosts(largePosts)
        assertEquals(100, postDao.getCount())
    }
}
