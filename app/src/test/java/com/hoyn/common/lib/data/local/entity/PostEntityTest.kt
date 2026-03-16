package com.hoyn.common.lib.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostEntityTest {

    @Test
    fun `PostEntity should hold all fields correctly`() {
        val entity = PostEntity(
            userId = 1,
            id = 100,
            title = "Test Title",
            body = "Test Body",
            cachedAt = 1700000000000L
        )

        assertEquals(1, entity.userId)
        assertEquals(100, entity.id)
        assertEquals("Test Title", entity.title)
        assertEquals("Test Body", entity.body)
        assertEquals(1700000000000L, entity.cachedAt)
    }

    @Test
    fun `PostEntity equals should compare all fields`() {
        val entity1 = PostEntity(userId = 1, id = 1, title = "Title", body = "Body", cachedAt = 1000L)
        val entity2 = PostEntity(userId = 1, id = 1, title = "Title", body = "Body", cachedAt = 1000L)
        val entity3 = PostEntity(userId = 1, id = 1, title = "Title", body = "Body", cachedAt = 2000L)

        assertEquals(entity1, entity2)
        assertFalse(entity1 == entity3)
    }

    @Test
    fun `PostEntity copy should work correctly`() {
        val original = PostEntity(
            userId = 1,
            id = 1,
            title = "Original",
            body = "Body",
            cachedAt = 1000L
        )

        val copied = original.copy(cachedAt = 2000L)

        assertEquals(original.userId, copied.userId)
        assertEquals(original.id, copied.id)
        assertEquals(original.title, copied.title)
        assertEquals(original.body, copied.body)
        assertEquals(2000L, copied.cachedAt)
    }

    @Test
    fun `PostEntity should handle special characters`() {
        val specialTitle = "Title: !@#\$%^&*()"
        val specialBody = "Body with newline\nand tab\t"

        val entity = PostEntity(
            userId = 1,
            id = 1,
            title = specialTitle,
            body = specialBody,
            cachedAt = 0L
        )

        assertEquals(specialTitle, entity.title)
        assertEquals(specialBody, entity.body)
    }

    @Test
    fun `PostEntity should handle empty strings`() {
        val entity = PostEntity(
            userId = 0,
            id = 0,
            title = "",
            body = "",
            cachedAt = 0L
        )

        assertEquals("", entity.title)
        assertEquals("", entity.body)
    }

    @Test
    fun `PostEntity hashCode should be consistent`() {
        val entity1 = PostEntity(userId = 1, id = 1, title = "Title", body = "Body", cachedAt = 1000L)
        val entity2 = PostEntity(userId = 1, id = 1, title = "Title", body = "Body", cachedAt = 1000L)

        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun `PostEntity toString should contain all fields`() {
        val entity = PostEntity(
            userId = 1,
            id = 100,
            title = "Test",
            body = "Body",
            cachedAt = 1700000000000L
        )

        val string = entity.toString()

        assertTrue(string.contains("userId=1"))
        assertTrue(string.contains("id=100"))
        assertTrue(string.contains("title=Test"))
        assertTrue(string.contains("body=Body"))
        assertTrue(string.contains("cachedAt=1700000000000"))
    }

    @Test
    fun `PostEntity component functions should work for destructuring`() {
        val entity = PostEntity(
            userId = 1,
            id = 100,
            title = "Title",
            body = "Body",
            cachedAt = 1700000000000L
        )

        val (userId, id, title, body, cachedAt) = entity

        assertEquals(1, userId)
        assertEquals(100, id)
        assertEquals("Title", title)
        assertEquals("Body", body)
        assertEquals(1700000000000L, cachedAt)
    }

    @Test
    fun `PostEntity should handle max int values`() {
        val entity = PostEntity(
            userId = Int.MAX_VALUE,
            id = Int.MAX_VALUE,
            title = "Title",
            body = "Body",
            cachedAt = Long.MAX_VALUE
        )

        assertEquals(Int.MAX_VALUE, entity.userId)
        assertEquals(Int.MAX_VALUE, entity.id)
        assertEquals(Long.MAX_VALUE, entity.cachedAt)
    }
}
