package com.hoyn.common.lib.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostTest {

    @Test
    fun `should create Post with all fields`() {
        val post = Post(
            userId = 1,
            id = 100,
            title = "Test Title",
            body = "Test Body"
        )

        assertEquals(1, post.userId)
        assertEquals(100, post.id)
        assertEquals("Test Title", post.title)
        assertEquals("Test Body", post.body)
    }

    @Test
    fun `should handle empty strings`() {
        val post = Post(
            userId = 0,
            id = 0,
            title = "",
            body = ""
        )

        assertEquals("", post.title)
        assertEquals("", post.body)
    }

    @Test
    fun `should handle special characters in title and body`() {
        val specialTitle = "Title with special chars: !@#\$%^&*()_+-=[]{}|;':\",./<>?"
        val specialBody = "Body with unicode: \u4e2d\u6587 \u65e5\u672c\u8a9e \ud83d\ude00\ud83d\udc94"

        val post = Post(
            userId = 1,
            id = 1,
            title = specialTitle,
            body = specialBody
        )

        assertEquals(specialTitle, post.title)
        assertEquals(specialBody, post.body)
    }

    @Test
    fun `should handle very long strings`() {
        val longTitle = "A".repeat(1000)
        val longBody = "B".repeat(5000)

        val post = Post(
            userId = 1,
            id = 1,
            title = longTitle,
            body = longBody
        )

        assertEquals(1000, post.title.length)
        assertEquals(5000, post.body.length)
    }

    @Test
    fun `equals should work correctly`() {
        val post1 = Post(userId = 1, id = 1, title = "Title", body = "Body")
        val post2 = Post(userId = 1, id = 1, title = "Title", body = "Body")
        val post3 = Post(userId = 1, id = 1, title = "Different", body = "Body")

        assertEquals(post1, post2)
        assertFalse(post1 == post3)
    }

    @Test
    fun `copy should work correctly`() {
        val original = Post(userId = 1, id = 1, title = "Original", body = "Body")

        val copied = original.copy(title = "Modified")

        assertEquals(original.userId, copied.userId)
        assertEquals(original.id, copied.id)
        assertEquals("Modified", copied.title)
        assertEquals(original.body, copied.body)
    }

    @Test
    fun `hashCode should be consistent`() {
        val post1 = Post(userId = 1, id = 1, title = "Title", body = "Body")
        val post2 = Post(userId = 1, id = 1, title = "Title", body = "Body")

        assertEquals(post1.hashCode(), post2.hashCode())
    }

    @Test
    fun `toString should contain all fields`() {
        val post = Post(userId = 1, id = 100, title = "Test", body = "Body")

        val string = post.toString()

        assertTrue(string.contains("userId=1"))
        assertTrue(string.contains("id=100"))
        assertTrue(string.contains("title=Test"))
        assertTrue(string.contains("body=Body"))
    }

    @Test
    fun `component functions should work for destructuring`() {
        val post = Post(userId = 1, id = 100, title = "Title", body = "Body")

        val (userId, id, title, body) = post

        assertEquals(1, userId)
        assertEquals(100, id)
        assertEquals("Title", title)
        assertEquals("Body", body)
    }
}
