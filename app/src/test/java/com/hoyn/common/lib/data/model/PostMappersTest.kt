package com.hoyn.common.lib.data.model

import com.hoyn.common.lib.data.local.entity.PostEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostMappersTest {

    @Test
    fun `toEntity should map Post to PostEntity with correct cachedAt`() {
        val post = Post(userId = 1, id = 100, title = "Test Title", body = "Test Body")
        val cachedAt = 1700000000000L

        val entity = post.toEntity(cachedAt)

        assertEquals(post.userId, entity.userId)
        assertEquals(post.id, entity.id)
        assertEquals(post.title, entity.title)
        assertEquals(post.body, entity.body)
        assertEquals(cachedAt, entity.cachedAt)
    }

    @Test
    fun `toEntity should preserve all Post fields`() {
        val post = Post(
            userId = Int.MAX_VALUE,
            id = 999999,
            title = "A very long title with special characters: !@#\$%^&*()",
            body = "Multi-line\nbody\nwith\nnewlines"
        )

        val entity = post.toEntity(0L)

        assertEquals(post.userId, entity.userId)
        assertEquals(post.id, entity.id)
        assertEquals(post.title, entity.title)
        assertEquals(post.body, entity.body)
    }

    @Test
    fun `toEntity should handle empty strings`() {
        val post = Post(userId = 0, id = 0, title = "", body = "")

        val entity = post.toEntity(System.currentTimeMillis())

        assertEquals("", entity.title)
        assertEquals("", entity.body)
    }

    @Test
    fun `toDomain should map PostEntity to Post correctly`() {
        val entity = PostEntity(
            userId = 2,
            id = 200,
            title = "Entity Title",
            body = "Entity Body",
            cachedAt = 1700000000000L
        )

        val post = entity.toDomain()

        assertEquals(entity.userId, post.userId)
        assertEquals(entity.id, post.id)
        assertEquals(entity.title, post.title)
        assertEquals(entity.body, post.body)
    }

    @Test
    fun `toDomain should ignore cachedAt field`() {
        val entity1 = PostEntity(userId = 1, id = 1, title = "Title", body = "Body", cachedAt = 100L)
        val entity2 = PostEntity(userId = 1, id = 1, title = "Title", body = "Body", cachedAt = 200L)

        val post1 = entity1.toDomain()
        val post2 = entity2.toDomain()

        assertEquals(post1, post2)
    }

    @Test
    fun `round-trip conversion should preserve data`() {
        val originalPost = Post(userId = 42, id = 123, title = "Round Trip", body = "Test Body")
        val cachedAt = System.currentTimeMillis()

        val entity = originalPost.toEntity(cachedAt)
        val convertedPost = entity.toDomain()

        assertEquals(originalPost, convertedPost)
    }

    @Test
    fun `toEntity with different cachedAt values should produce different entities`() {
        val post = Post(userId = 1, id = 1, title = "Title", body = "Body")

        val entity1 = post.toEntity(100L)
        val entity2 = post.toEntity(200L)

        assertFalse(entity1 == entity2)
        assertTrue(entity1.userId == entity2.userId)
        assertTrue(entity1.id == entity2.id)
    }
}
