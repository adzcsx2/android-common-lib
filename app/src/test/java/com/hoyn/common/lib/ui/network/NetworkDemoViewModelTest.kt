package com.hoyn.common.lib.ui.network

import android.app.Application
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkDemoViewModelTest {

    private lateinit var mockRepository: PostRepository
    private lateinit var mockApplication: Application
    private lateinit var viewModel: NetworkDemoViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val samplePosts = listOf(
        Post(userId = 1, id = 1, title = "Post 1", body = "Body 1"),
        Post(userId = 2, id = 2, title = "Post 2", body = "Body 2")
    )

    @Before
    fun setup() {
        mockRepository = mock()
        mockApplication = mock()
        viewModel = NetworkDemoViewModel(mockApplication, mockRepository, testDispatcher)
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadPosts should emit Success when repository succeeds`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.success(PostLoadResult(posts = samplePosts, isFromCache = false, updatedAt = 1000L))
        )

        viewModel.loadPosts()

        verify(mockRepository).getPosts()
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Success)
        assertEquals(samplePosts, (finalState as UIState.Success<*>).data)
    }

    @Test
    fun `loadPosts should set isFromCache to true when data is from cache`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.success(PostLoadResult(posts = samplePosts, isFromCache = true, updatedAt = 1000L))
        )
        viewModel.loadPosts()
        assertTrue(viewModel.isFromCache.value)
    }

    @Test
    fun `loadPosts should set isFromCache to false when data is from network`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.success(PostLoadResult(posts = samplePosts, isFromCache = false, updatedAt = 1000L))
        )
        viewModel.loadPosts()
        assertFalse(viewModel.isFromCache.value)
    }

    @Test
    fun `loadPosts should emit Empty when repository returns empty list`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.success(PostLoadResult(posts = emptyList(), isFromCache = false, updatedAt = 1000L))
        )
        viewModel.loadPosts()
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Empty)
    }

    @Test
    fun `loadPosts should emit Error when repository fails`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(
            Result.failure(Exception("Network error"))
        )

        viewModel.loadPosts()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue((finalState as UIState.Error).message.contains("Network error"))
    }

    @Test
    fun `clearCache should call repository clearCache`() = runTest {
        viewModel.clearCache()
        verify(mockRepository).clearCache()
    }

    @Test
    fun `loadPosts should handle cached data correctly`() = runTest {
        val cachedPosts = listOf(
            Post(userId = 1, id = 1, title = "Cached Post", body = "Cached Body")
        )
        whenever(mockRepository.getPosts()).thenReturn(Result.success(PostLoadResult(posts = cachedPosts, isFromCache = true, updatedAt = 2000L))
        )
        viewModel.loadPosts()
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Success)
        assertTrue(viewModel.isFromCache.value)
    }

    @Test
    fun `loadPosts should handle large data sets`() = runTest {
        val largePosts = (1..100).map { id ->
            Post(userId = id, id = id, title = "Title $id", body = "Body $id")
        }.toList()
        whenever(mockRepository.getPosts()).thenReturn(Result.success(
            PostLoadResult(posts = largePosts, isFromCache = false, updatedAt = System.currentTimeMillis())
        ))

        viewModel.loadPosts()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Success)
        assertEquals(100, (finalState as UIState.Success<List<Post>>).data.size)
    }
}
