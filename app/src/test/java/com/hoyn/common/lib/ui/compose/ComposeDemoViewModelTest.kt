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
        mockRepository = mock()
        whenever(mockApplication.applicationContext).thenReturn(mock<Context>())
        viewModel = ComposeDemoViewModel(mockApplication, mockRepository, testDispatcher)
    }

    @Test
    fun initial_state_should_be_Loading() = runTest {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadPosts_should_emit_Success_when_repository_succeeds() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(PostLoadResult(posts = samplePosts, isFromCache = false, updatedAt = 1000L))
        )

        viewModel.loadPosts()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Success)
        assertEquals(samplePosts, (finalState as UIState.Success<List<Post>>).data)
    }

    @Test
    fun loadPosts_should_set_isFromCache_true_when_data_is_from_cache() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(PostLoadResult(posts = samplePosts, isFromCache = true, updatedAt = 1000L))
        )
        viewModel.loadPosts()
        assertTrue(viewModel.isFromCache.value)
    }

    @Test
    fun loadPosts_should_set_isFromCache_false_when_data_is_from_network() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(PostLoadResult(posts = samplePosts, isFromCache = false, updatedAt = 1000L))
        )
        viewModel.loadPosts()
        assertFalse(viewModel.isFromCache.value)
    }

    @Test
    fun loadPosts_should_emit_Empty_when_repository_returns_empty_list() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(PostLoadResult(posts = emptyList(), isFromCache = false, updatedAt = 1000L))
        )

        viewModel.loadPosts()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Empty)
    }

    @Test
    fun loadPosts_should_emit_Error_when_repository_fails() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(Result.failure(Exception("Connection timeout")))

        viewModel.loadPosts()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue((finalState as UIState.Error).message.contains("Connection timeout"))
    }

    @Test
    fun loadPosts_should_handle_large_data_sets() = runTest {
        val largePosts = (1..100).map { id ->
            Post(userId = id, id = id, title = "Title $id", body = "Body $id")
        }.toList()
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(PostLoadResult(posts = largePosts, isFromCache = false, updatedAt = System.currentTimeMillis()))
        )

        viewModel.loadPosts()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Success)
        assertEquals(100, (finalState as UIState.Success<List<Post>>).data.size)
    }
}
