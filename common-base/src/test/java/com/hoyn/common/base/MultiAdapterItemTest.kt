package com.hoyn.common.base

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MultiAdapterItemTest {

    @Test
    fun `construction with data and viewType`() {
        val item = MultiAdapterItem(listOf("a", "b"), 1)
        assertEquals(listOf("a", "b"), item.data)
        assertEquals(1, item.viewType)
    }

    @Test
    fun `empty data list is valid`() {
        val item = MultiAdapterItem<String>(emptyList(), 2)
        assertEquals(emptyList<String>(), item.data)
        assertEquals(2, item.viewType)
    }

    @Test
    fun `equality - same data and viewType`() {
        val item1 = MultiAdapterItem(listOf(1, 2), 10)
        val item2 = MultiAdapterItem(listOf(1, 2), 10)
        assertEquals(item1, item2)
    }

    @Test
    fun `inequality - different viewType`() {
        val item1 = MultiAdapterItem(listOf("x"), 1)
        val item2 = MultiAdapterItem(listOf("x"), 2)
        assertNotEquals(item1, item2)
    }

    @Test
    fun `inequality - different data`() {
        val item1 = MultiAdapterItem(listOf("x"), 1)
        val item2 = MultiAdapterItem(listOf("y"), 1)
        assertNotEquals(item1, item2)
    }

    @Test
    fun `copy preserves data and allows override`() {
        val item = MultiAdapterItem(listOf("a"), 1)
        val copied = item.copy(viewType = 2)
        assertEquals(listOf("a"), copied.data)
        assertEquals(2, copied.viewType)
    }

    @Test
    fun `hashCode consistent with equals`() {
        val item1 = MultiAdapterItem(listOf("test"), 5)
        val item2 = MultiAdapterItem(listOf("test"), 5)
        assertEquals(item1.hashCode(), item2.hashCode())
    }

    @Test
    fun `works with custom data class`() {
        data class Comment(val id: Int, val text: String)

        val comment = Comment(1, "hello")
        val item = MultiAdapterItem(listOf(comment), 100)
        assertEquals(1, item.data.size)
        assertEquals(comment, item.data.first())
        assertEquals(100, item.viewType)
    }
}
