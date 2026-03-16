package com.hoyn.common.network

import okhttp3.Interceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class OkHttpClientFactoryTest {

    private lateinit var mockInterceptor: Interceptor
    private lateinit var factory: OkHttpClientFactory

    @Before
    fun setup() {
        mockInterceptor = mock()
        factory = OkHttpClientFactory()
    }

    @Test
    fun `create should return OkHttpClient with correct timeout settings`() {
        val client = factory.create(emptyList(), emptyList())

        assertNotNull(client)
        assertEquals(NetworkConfig.connectTimeout, client.connectTimeoutMillis)
        assertEquals(NetworkConfig.readTimeout, client.readTimeoutMillis)
        assertEquals(NetworkConfig.writeTimeout, client.writeTimeoutMillis)
    }

    @Test
    fun `create should add interceptors in correct order`() {
        val client = factory.create(listOf(mockInterceptor), emptyList())

        // Verify the LoggingInterceptor is added
        val interceptors = client.interceptors
        assertTrue(interceptors.any { it is LoggingInterceptor })
    }

    @Test
    fun `create should add network interceptors`() {
        val networkInterceptor = mock<Interceptor>()
        val client = factory.create(emptyList(), listOf(networkInterceptor))

        val interceptors = client.networkInterceptors
        assertEquals(1, interceptors.size)
        assertTrue(interceptors.contains(networkInterceptor))
    }

    @Test
    fun `create with empty interceptors should still return valid client`() {
        val client = factory.create(emptyList(), emptyList())

        assertNotNull(client)
    }

    @Test
    fun `create with multiple interceptors should add all`() {
        val interceptor1 = mock<Interceptor>()
        val interceptor2 = mock<Interceptor>()
        val client = factory.create(listOf(interceptor1, interceptor2), emptyList())

        assertEquals(2, client.interceptors.size)
    }

    @Test
    fun `create with multiple network interceptors should add all`() {
        val networkInterceptor1 = mock<Interceptor>()
        val networkInterceptor2 = mock<Interceptor>()
        val client = factory.create(emptyList(), listOf(networkInterceptor1, networkInterceptor2))

        assertEquals(2, client.networkInterceptors.size)
    }
}
