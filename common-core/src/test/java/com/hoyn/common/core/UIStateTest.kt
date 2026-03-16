package com.hoyn.common.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
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
    fun `Success state getDataOrNull should return null for non-Success`() {
        val success = UIState.Success("test")
        assertNull(success.getDataOrNull())
        assertEquals("test", success.getDataOrNull())
    }

    @Test
    fun `Success state getDataOrNull should return data for non-Success`() {
        val success = UIState.Success("test")
        assertNull(success.getDataOrNull())
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
    fun `Error state getErrorMsgOrNull should return message for non-Error`() {
        val error = UIState.Error(500, "Server Error")
        assertEquals("Server Error", error.getErrorMsgOrNull())
    }

    @Test
    fun `Error state getDataOrNull should return null for non-Error`() {
        val error = UIState.Error(500, "Server Error")
        assertNull(error.getDataOrNull())
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
    fun `Empty state getDataOrNull should return null`() {
        val empty = UIState.Empty
        assertNull(empty.getDataOrNull())
    }

    @Test
    fun `Empty state getErrorMsgOrNull should return null for non-Empty`() {
        val empty = UIState.Empty
        assertNull(empty.getDataOrNull())
        assertNull(empty.getErrorMsgOrNull())
    }

    @Test
    fun `loading factory method should return Loading state`() {
        val loading = UIState.loading()
        assertTrue(loading is Loading)
    }

    @Test
    fun `success factory method should return Success state`() {
        val data = listOf("test1", "test2")
        val success = UIState.Success(data)
        assertEquals(data, success.data)
    }

    @Test
    fun `error factory method should return Error state`() {
        val error = UIState.error(404, "Not Found")
        assertEquals(404, error.code)
        assertEquals("Not Found", error.message)
    }

    @Test
    fun `empty factory method should return Empty state`() {
        val empty = UIState.Empty
        assertTrue(empty.isEmpty)
    }
}
