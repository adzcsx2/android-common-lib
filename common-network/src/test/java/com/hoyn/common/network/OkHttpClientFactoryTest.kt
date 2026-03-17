package com.hoyn.common.network

import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class OkHttpClientFactoryTest {

    @Test
    fun `create should return OkHttpClient with correct timeout settings`() {
        val client = OkHttpClientFactory.create()

        assertNotNull(client)
        assertEquals(NetworkConfig.connectTimeout, TimeUnit.MILLISECONDS.toSeconds(client.connectTimeoutMillis.toLong()))
        assertEquals(NetworkConfig.readTimeout, TimeUnit.MILLISECONDS.toSeconds(client.readTimeoutMillis.toLong()))
        assertEquals(NetworkConfig.writeTimeout, TimeUnit.MILLISECONDS.toSeconds(client.writeTimeoutMillis.toLong()))
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
