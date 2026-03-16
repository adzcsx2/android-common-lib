package com.hoyn.common.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UIStateTest {

    @Test
    fun `Loading state should be loading`() {
        val loading = UIState.Loading
        assertTrue(loading.isLoading)
        assertFalse(loading.isSuccess)
        assertFalse(loading.isError)
        assertFalse(loading.isEmpty)
    }

    @Test
    fun `Success state should hold data correctly`() {
        val data = listOf("test1", "test2")
        val success = UIState.Success(data)
        assertEquals(data, success.data)
        assertTrue(success.isSuccess)
        assertFalse(success.isError)
        assertFalse(success.isEmpty)
    }

    @Test
    fun `Error state should hold error info correctly`() {
        val error = UIState.Error(404, "Not Found")
        assertEquals(404, error.code)
        assertEquals("Not Found", error.message)
        assertTrue(error.isError)
        assertFalse(error.isSuccess)
        assertFalse(error.isLoading)
        assertFalse(error.isEmpty)
    }

    @Test
    fun `Empty state should be empty`() {
        val empty = UIState.Empty
        assertTrue(empty.isEmpty)
        assertFalse(empty.isLoading)
        assertFalse(empty.isSuccess)
        assertFalse(empty.isError)
    }

    @Test
    fun `getDataOrNull should return data for Success state`() {
        val data = "test data"
        val success = UIState.Success(data)
        assertEquals(data, success.getDataOrNull())
    }

    @Test
    fun `getDataOrNull should return null for non-Success states`() {
        val loading = UIState.Loading
        assertEquals(null, loading.getDataOrNull())

        val error = UIState.Error(500, "Error")
        assertEquals(null, error.getDataOrNull())

        val empty = UIState.Empty
        assertEquals(null, empty.getDataOrNull())
    }
}
