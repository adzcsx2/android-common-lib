package com.hoyn.common.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResponseTest {

    @Test
    fun `success factory method should create success response`() {
        val data = mapOf("key1" to "value1", "key2" to "value2")
        val response = ApiResponse.success(data)

        assertTrue(response.isSuccess)
        assertEquals(0, response.code)
        assertEquals("success", response.message)
        assertEquals(data, response.data)
    }

    @Test
    fun `error factory method should create error response`() {
        val response = ApiResponse.error(404, "Not Found")

        assertFalse(response.isSuccess)
        assertEquals(404, response.code)
        assertEquals("Not Found", response.message)
        assertNull(response.data)
    }

    @Test
    fun `success response should return correct data`() {
        val data = "test data"
        val response = ApiResponse.Success(data)

        assertEquals(data, response.data)
    }

    @Test
    fun `error response should return null data`() {
        val response = ApiResponse.Error(500, "Server Error")

        assertNull(response.data)
    }

    @Test
    fun `code should return correct value`() {
        val successResponse = ApiResponse.Success("data")
        assertEquals(0, successResponse.code)

        val errorResponse = ApiResponse.Error(404, "Not Found")
        assertEquals(404, errorResponse.code)
    }

    @Test
    fun `message should return correct value`() {
        val successResponse = ApiResponse.Success("data")
        assertEquals("success", successResponse.message)

        val errorResponse = ApiResponse.Error(500, "Server Error")
        assertEquals("Server Error", errorResponse.message)
    }

    @Test
    fun `isSuccess should return true for success response`() {
        val successResponse = ApiResponse.Success("data")
        assertTrue(successResponse.isSuccess)
    }

    @Test
    fun `isSuccess should return false for error response`() {
        val errorResponse = ApiResponse.Error(500, "Server Error")
        assertFalse(errorResponse.isSuccess)
    }

    @Test
    fun `data should be nullable for error response`() {
        val errorResponse = ApiResponse.Error(500, "Server Error")
        assertNull(errorResponse.data)
    }

    @Test
    fun `data should be non-null for success response`() {
        val data = mapOf("key" to "value")
        val successResponse = ApiResponse.Success(data)
        assertNotNull(successResponse.data)
    }

    @Test
    fun `should handle complex data types`() {
        data class User(val id: Int, val name: String)
        val user = User(1, "Test User")
        val response = ApiResponse.success(user)

        assertTrue(response.isSuccess)
        assertEquals(user, response.data)
    }

    @Test
    fun `should handle null data in success response`() {
        val response = ApiResponse.Success<String?>(null)
        assertTrue(response.isSuccess)
        assertNull(response.data)
    }
}
