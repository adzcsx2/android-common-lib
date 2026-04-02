package com.hoyn.common.network

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResponseTest {

    private val gson = Gson()

    @Test
    fun `isSuccess returns true when code is zero`() {
        val response = ApiResponse(code = 0, message = "ok", data = "payload")

        assertTrue(response.isSuccess())
    }

    @Test
    fun `isSuccess returns false when code is non-zero`() {
        val response = ApiResponse(code = 500, message = "error", data = "payload")

        assertFalse(response.isSuccess())
    }

    @Test
    fun `success factory creates zero-code response`() {
        val response = ApiResponse.success("payload")

        assertEquals(0, response.code())
        assertEquals("success", response.msg())
        assertEquals("payload", response.data())
    }

    @Test
    fun `error factory creates response with null data`() {
        val response = ApiResponse.error<String>(400, "bad request")

        assertEquals(400, response.code())
        assertEquals("bad request", response.msg())
        assertNull(response.data())
    }

    @Test
    fun `deserialize supports message field`() {
        val json = """{"code":0,"message":"ok","data":"payload"}"""

        val response = gson.fromJson(json, ApiResponse::class.java)

        assertEquals(0, response.code())
        assertEquals("ok", response.msg())
    }

    @Test
    fun `deserialize supports msg field`() {
        val json = """{"code":0,"msg":"ok","data":"payload"}"""

        val response = gson.fromJson(json, ApiResponse::class.java)

        assertEquals(0, response.code())
        assertEquals("ok", response.msg())
    }
}
