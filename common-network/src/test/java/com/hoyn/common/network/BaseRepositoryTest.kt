package com.hoyn.common.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class BaseRepositoryTest {

    private lateinit var mockClient: OkHttpClient
    private lateinit var repository: TestRepository

    @Before
    fun setup() {
        mockClient = mock()
        repository = TestRepository(mockClient)
    }

    @Test
    fun `api should return lazy created service`() {
        val api = repository.api
        assertNotNull(api)
    }

    @Test
    fun `api should return same instance on multiple calls`() {
        val api1 = repository.api
        val api2 = repository.api
        assertEquals(api1, api2)
    }

    // Test implementation
    class TestRepository(mockClient) : BaseRepository<TestApi>(mockClient) {
        override val api: TestApi = mockClient.create(TestApi::class.java)
    }

    // Test API interface
    interface TestApi {
        suspend fun testMethod(): String
    }
}
