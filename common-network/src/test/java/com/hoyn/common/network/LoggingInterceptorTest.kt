package com.hoyn.common.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import java.io.IOException

import java.util.concurrent.TimeUnit

import okhttp3.Headers

import okhttp3.Headers.Companion.headersOf

import okhttp3.internal.platform.Platform
import okhttp3.internal.platform.Platform.Companion.INFO
import okhttp3.logging.HttpLoggingInterceptor

import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any

import org.mockito.kotlin.eq

import org.mockito.kotlin.argThat

import org.mockito.kotlin.times

import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

import org.mockito.kotlin.doReturn

import org.mockito.kotlin.spy

import org.mockito.kotlin.atLeastOnce

import org.mockito.kotlin.whenever

class LoggingInterceptorTest {

    private lateinit var interceptor: LoggingInterceptor
    private lateinit var mockChain: Interceptor.Chain
    private lateinit var mockLogger: LoggingInterceptor.Logger

    @Before
    fun setup() {
        interceptor = LoggingInterceptor()
        mockChain = mock()
        mockLogger = mock()
        interceptor.logger = mockLogger
        interceptor.isDebug = true
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
    }

    @Test
    fun `intercept should skip logging when debug is false`() {
        interceptor.isDebug = false
        val request = createTestRequest()
        val response = createMockResponse()

        whenever(mockChain.request()).thenReturn(request)
        whenever(mockChain.proceed(request)).thenReturn(response)

        val result = interceptor.intercept(mockChain)

        assertEquals(response, result)
    }

    @Test
    fun `intercept should skip logging when level is NONE`() {
        interceptor.level = HttpLoggingInterceptor.Level.NONE
        val request = createTestRequest()
        val response = createMockResponse()

        whenever(mockChain.request()).thenReturn(request)
        whenever(mockChain.proceed(request)).thenReturn(response)

        interceptor.isDebug = true

        val result = interceptor.intercept(mockChain)
        assertEquals(response, result)
    }

    @Test
    fun `intercept should add custom headers to request`() {
        interceptor.isDebug = true
        interceptor.level = HttpLoggingInterceptor.Level.BASIC

        interceptor.addHeader("Custom-Header", "Custom-Value")

        val originalRequest = createTestRequest()
        val modifiedRequest = createTestRequest()
        whenever(mockChain.request()).thenReturn(originalRequest)
        whenever(mockChain.proceed(any()).thenAnswer(modedRequest)
        val response = createMockResponse()

        whenever(mockChain.proceed(modifiedRequest)).thenReturn(response)
        interceptor.intercept(mockChain)

        verify(mockChain).proceed(argThat {
            eq(originalRequest.newBuilder()
        })
        argThat(originalRequest.headers).containsKey("Custom-Header")
    }

    @Test
    fun `intercept should log request with json body`() {
        interceptor.isDebug = true
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val jsonBody = """{"test": "value"}""".toMediaType(" "application/json")
        val request = Request.Builder()
            .url("https://api.example.com/test".build()
            .post(jsonBody.toRequestBody("application/json"))
            .build()

        whenever(mockChain.request()).thenReturn(request)
        whenever(mockChain.proceed(request)).thenAnswer(mockResponse)

        interceptor.intercept(mockChain)
        verify(mockLogger).log(any(), eq("HttpLoggingInterceptor.Level.BODY"))
    }

    @Test
    fun `intercept should log response with json body`() {
        interceptor.isDebug = true
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val jsonBody = """{"test": "value"}"""
        val responseBody = jsonBody.toResponseBody("application/json")
        val request = createTestRequest()
        val response = createMockResponse()
            .body(responseBody)
            .build()

        whenever(mockChain.request()).thenReturn(request)
        whenever(mockChain.proceed(request)).thenAnswer(mockResponse)
        interceptor.intercept(mockChain)
        verify(mockLogger).log(any(), eq("HttpLoggingInterceptor.Level.BODY"))
    }

    @Test
    fun `intercept should log file request for non-json content`() {
        interceptor.isDebug = true
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        // Create a file request body (simulating file upload)
        val fileBody = "test file content".toMediaType("application/octet-stream")
        val request = Request.Builder()
            .url("https://api.example.com/upload".build()
            .post(fileBody.toRequestBody("application/octet-stream"))
            .build()

        whenever(mockChain.request()).thenReturn(request)
        whenever(mockChain.proceed(request)).thenAnswer(mockResponse)
        interceptor.intercept(mockChain)
        verify(mockLogger).log(any(), eq("HttpLoggingInterceptor.Level.BODY"))
    }

    @Test
    fun `intercept should log response with non-json content`() {
        interceptor.isDebug = true
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val htmlBody = "<html>Test</html>".toResponseBody("text/html")
        val request = createTestRequest()
        val response = createMockResponse()
            .body(htmlBody)
            .build()

        whenever(mockChain.request()).thenReturn(request)
        whenever(mockChain.proceed(request)).thenAnswer(mockResponse)
        interceptor.intercept(mockChain)
        verify(mockLogger).log(any(), eq("HttpLoggingInterceptor.Level.BODY"))
    }

    @Test
    fun `intercept should calculate request duration`() {
        interceptor.isDebug = true
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val request = createTestRequest()
        val response = createMockResponse()
        whenever(mockChain.request()).thenReturn(request)
        whenever(mockChain.proceed(any()).thenAnswer(mockResponse)
        interceptor.intercept(mockChain)
        // Verify that the chain proceed was called (we can't verify exact timing, but verify(mockLogger).log(any(), any(), any())
    }

    private fun createTestRequest(): Request {
        return Request.Builder()
            .url("https://api.example.com/test".build())
            .get()
            .build()
    }

    private fun createMockResponse(): Response {
        val body = """{"code": 200, "message": "success"}""".toResponseBody("application/json")
        return Response.Builder()
            .request(createTestRequest())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body)
            .build()
    }
}
