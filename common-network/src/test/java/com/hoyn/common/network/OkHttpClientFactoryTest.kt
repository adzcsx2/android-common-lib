package com.hoyn.common.network

import okhttp3.OkHttpClient
import org.junit.Assert.assertNotNull
import org.junit.Test

class OkHttpClientFactoryTest {

    @Test
    fun `create should return OkHttpClient with correct timeout settings`() {
        val client = OkHttpClientFactory.create()

        assertNotNull(client)
        assertEquals(NetworkConfig.connectTimeout, client.connectTimeoutMillis / 1000)
        assertEquals(NetworkConfig.readTimeout, client.readTimeoutMillis / 1000)
        assertEquals(NetworkConfig.writeTimeout, client.writeTimeoutMillis / 1000)
    }

    @Test
    fun `create should add logging interceptor`() {
        val client = OkHttpClientFactory.create()

        val interceptors = client.interceptors
        assertTrue(interceptors.any { it is LoggingInterceptor })
    }

    @Test
    fun `create with empty interceptors should return valid client`() {
        val client = OkHttpClientFactory.create(emptyList(), emptyList())

        assertNotNull(client)
    }
}
