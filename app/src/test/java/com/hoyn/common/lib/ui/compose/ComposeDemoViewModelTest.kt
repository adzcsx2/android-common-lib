package com.hoyn.common.lib.ui.compose

import android.app.Application
import android.content.Context
import com.hoyn.common.core.UIState
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.repository.PostLoadResult
import com.hoyn.common.lib.data.repository.PostRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeDemoViewModelTest {

    private lateinit var mockApplication: Application
    private lateinit var mockContext: Context
    private lateinit var mockRepository: PostRepository
    private lateinit var viewModel: ComposeDemoViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val samplePosts = listOf(
        Post(userId = 1, id = 1, title = "Compose Post 1", body = "Body 1"),
        Post(userId = 2, id = 2, title = "Compose Post 2", body = "Body 2")
    )

    @Before
    fun setup() {
        mockApplication = mock()
        mockContext = mock()
        mockRepository = mock()
        whenever(mockApplication.applicationContext).thenReturn(mockContext)
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        viewModel = ComposeDemoViewModel(mockApplication, mockRepository, testDispatcher)

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadPosts should emit Success when repository succeeds`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.success(
            PostLoadResult(posts = samplePosts, isFromCache = false, updatedAt = 1000L)
        ))

        viewModel = ComposeDemoViewModel(mockApplication, mockRepository, testDispatcher)

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isSuccess)
        assertEquals(samplePosts, (finalState as UIState.Success).data)
    }

    @Test
    fun `loadPosts should emit Empty when repository returns empty list`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.success(
            PostLoadResult(posts = emptyList(), isFromCache = false, updatedAt = 1000L)
        ))

        viewModel = ComposeDemoViewModel(mockApplication, mockRepository, testDispatcher)

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isEmpty)
    }

    @Test
    fun `loadPosts should emit Error when repository fails`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.failure(Exception("Compose error")))

        viewModel = ComposeDemoViewModel(mockApplication, mockRepository, testDispatcher)

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isError)
    }

    @Test
    fun `isFromCache should reflect cache status correctly`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.success(
            PostLoadResult(posts = samplePosts, isFromCache = true, updatedAt = 1000L)
        ))

        viewModel = ComposeDemoViewModel(mockApplication, mockRepository, testDispatcher)

        assertTrue(viewModel.isFromCache.value)
    }

    @Test
    fun `isFromCache should be false for network data`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.success(
            PostLoadResult(posts = samplePosts, isFromCache = false, updatedAt = 1000L)
        ))

        viewModel = ComposeDemoViewModel(mockApplication, mockRepository, testDispatcher)

        assertFalse(viewModel.isFromCache.value)
    }

    @Test
    fun `loadPosts should handle large data sets`() = runTest {
        val largePosts = (1..100).map { id ->
            Post(userId = id, id = id, title = "Title $id", body = "Body $id")
        }
        whenever(mockRepository.getPosts()).thenReturn(Result.success(
            PostLoadResult(posts = largePosts, isFromCache = false, updatedAt = System.currentTimeMillis())
        ))

        viewModel = ComposeDemoViewModel(mockApplication, mockRepository, testDispatcher)

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isSuccess)
        assertEquals(100, (finalState as UIState.Success).data.size)
    }

    @Test
    fun `loadPosts should handle network failure gracefully`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.failure(Exception("Connection timeout")))

        viewModel = ComposeDemoViewModel(mockApplication, mockRepository, testDispatcher)

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isError)
        assertTrue(finalState.getErrorMsgOrNull()?.contains("Connection timeout") == true)
    }
}
