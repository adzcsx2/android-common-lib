package com.hoyn.common.lib.data.repository

import com.hoyn.common.lib.data.model.Post
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostLoadResultTest {

    @Test
    fun `PostLoadResult should hold posts correctly`() {
        val posts = listOf(
            Post(userId = 1, id = 1, title = "Title 1", body = "Body 1"),
            Post(userId = 2, id = 2, title = "Title 2", body = "Body 2")
        )

        val result = PostLoadResult(posts = posts, isFromCache = false, updatedAt = 1000L)

        assertEquals(2, result.posts.size)
        assertEquals(posts, result.posts)
    }

    @Test
    fun `PostLoadResult should indicate cache source correctly`() {
        val cachedResult = PostLoadResult(posts = emptyList(), isFromCache = true, updatedAt = 0L)
        val networkResult = PostLoadResult(posts = emptyList(), isFromCache = false, updatedAt = 0L)

        assertTrue(cachedResult.isFromCache)
        assertFalse(networkResult.isFromCache)
    }

    @Test
    fun `PostLoadResult should hold updatedAt timestamp`() {
        val timestamp = System.currentTimeMillis()

        val result = PostLoadResult(posts = emptyList(), isFromCache = false, updatedAt = timestamp)

        assertEquals(timestamp, result.updatedAt)
    }

    @Test
    fun `PostLoadResult should allow empty posts list`() {
        val result = PostLoadResult(posts = emptyList(), isFromCache = true, updatedAt = 0L)

        assertTrue(result.posts.isEmpty())
    }

    @Test
    fun `PostLoadResult copy should work correctly`() {
        val original = PostLoadResult(
            posts = listOf(Post(userId = 1, id = 1, title = "Title", body = "Body")),
            isFromCache = false,
            updatedAt = 1000L
        )

        val copied = original.copy(isFromCache = true)

        assertEquals(original.posts, copied.posts)
        assertTrue(copied.isFromCache)
        assertEquals(original.updatedAt, copied.updatedAt)
    }

    @Test
    fun `PostLoadResult equals should work correctly`() {
        val posts = listOf(Post(userId = 1, id = 1, title = "Title", body = "Body"))

        val result1 = PostLoadResult(posts = posts, isFromCache = true, updatedAt = 1000L)
        val result2 = PostLoadResult(posts = posts, isFromCache = true, updatedAt = 1000L)
        val result3 = PostLoadResult(posts = posts, isFromCache = false, updatedAt = 1000L)

        assertEquals(result1, result2)
        assertTrue(result1 != result3)
    }

    @Test
    fun `PostLoadResult should handle large posts list`() {
        val posts = (1..1000).map { id ->
            Post(userId = id, id = id, title = "Title $id", body = "Body $id")
        }

        val result = PostLoadResult(posts = posts, isFromCache = false, updatedAt = System.currentTimeMillis())

        assertEquals(1000, result.posts.size)
        assertEquals(1000, result.posts.last().id)
    }
}
