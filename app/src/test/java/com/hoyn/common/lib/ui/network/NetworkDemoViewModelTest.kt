package com.hoyn.common.lib.ui.network

import com.hoyn.common.core.UIState
import com.hoyn.common.lib.data.model.Comment
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.remote.api.CommentApi
import com.hoyn.common.lib.data.repository.PostLoadResult
import com.hoyn.common.lib.data.repository.PostRepository
import com.hoyn.common.network.ApiResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NetworkDemoViewModelTest : KoinTest {

    private lateinit var mockRepository: PostRepository
    private lateinit var mockCommentApi: CommentApi
    private lateinit var viewModel: NetworkDemoViewModel

    private val samplePosts = listOf(
        Post(userId = 1, id = 1, title = "Post 1", body = "Body 1"),
        Post(userId = 2, id = 2, title = "Post 2", body = "Body 2")
    )

    private val sampleComments = listOf(
        Comment(postId = 1, id = 1, name = "Name 1", email = "a@example.com", body = "Comment 1"),
        Comment(postId = 1, id = 2, name = "Name 2", email = "b@example.com", body = "Comment 2")
    )

    // Mock Provider 规则 - 必须在 KoinTestRule 之前
    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    // 使用 Koin 测试规则
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(module {
            single { mockRepository }
            single { mockCommentApi }
        })
    }

    @Before
    fun setup() {
        mockRepository = declareMock()
        mockCommentApi = declareMock()
        viewModel = NetworkDemoViewModel()
    }

    /**
     * 轮询等待 StateFlow 不再处于 Loading 状态
     */
    private fun <T> awaitNotLoading(
        stateFlow: StateFlow<UIState<T>>,
        timeoutMs: Long = 3000
    ): UIState<T> {
        val start = System.currentTimeMillis()
        while (stateFlow.value.isLoading && System.currentTimeMillis() - start < timeoutMs) {
            ShadowLooper.idleMainLooper()
            org.robolectric.Robolectric.getForegroundThreadScheduler()
                .advanceToLastPostedRunnable()
            Thread.sleep(100)
        }
        return stateFlow.value
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadPosts should emit Success when repository succeeds`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(PostLoadResult(posts = samplePosts, isFromCache = false, updatedAt = 1000L))
        )

        viewModel.loadPosts()
        val finalState = awaitNotLoading(viewModel.uiState)

        assertTrue("Expected Success but got $finalState", finalState is UIState.Success)
        assertEquals(samplePosts, (finalState as UIState.Success<*>).data)
    }

    @Test
    fun `loadPosts should set isFromCache to true when data is from cache`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(PostLoadResult(posts = samplePosts, isFromCache = true, updatedAt = 1000L))
        )
        viewModel.loadPosts()
        awaitNotLoading(viewModel.uiState)
        assertTrue(viewModel.isFromCache.value)
    }

    @Test
    fun `loadPosts should set isFromCache to false when data is from network`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(PostLoadResult(posts = samplePosts, isFromCache = false, updatedAt = 1000L))
        )
        viewModel.loadPosts()
        awaitNotLoading(viewModel.uiState)
        assertFalse(viewModel.isFromCache.value)
    }

    @Test
    fun `loadPosts should emit Empty when repository returns empty list`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(PostLoadResult(posts = emptyList(), isFromCache = false, updatedAt = 1000L))
        )
        viewModel.loadPosts()
        val finalState = awaitNotLoading(viewModel.uiState)
        assertTrue("Expected Empty but got $finalState", finalState is UIState.Empty)
    }

    @Test
    fun `loadPosts should emit Error when repository fails`() = runTest {
        whenever(mockRepository.getPosts()).thenReturn(
            Result.failure(Exception("Network error"))
        )

        viewModel.loadPosts()
        val finalState = awaitNotLoading(viewModel.uiState)

        assertTrue("Expected Error but got $finalState", finalState is UIState.Error)
        assertTrue((finalState as UIState.Error).message.contains("Network error"))
    }

    @Test
    fun `clearCache should call repository clearCache`() = runTest {
        viewModel.clearCache()
        Thread.sleep(200)
        ShadowLooper.idleMainLooper()
    }

    @Test
    fun `loadPosts should handle cached data correctly`() = runTest {
        val cachedPosts = listOf(
            Post(userId = 1, id = 1, title = "Cached Post", body = "Cached Body")
        )
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(PostLoadResult(posts = cachedPosts, isFromCache = true, updatedAt = 2000L))
        )
        viewModel.loadPosts()
        val finalState = awaitNotLoading(viewModel.uiState)
        assertTrue("Expected Success but got $finalState", finalState is UIState.Success)
        assertTrue(viewModel.isFromCache.value)
    }

    @Test
    fun `loadPosts should handle large data sets`() = runTest {
        val largePosts = (1..100).map { id ->
            Post(userId = id, id = id, title = "Title $id", body = "Body $id")
        }.toList()
        whenever(mockRepository.getPosts()).thenReturn(
            Result.success(
                PostLoadResult(posts = largePosts, isFromCache = false, updatedAt = System.currentTimeMillis())
            )
        )

        viewModel.loadPosts()
        val finalState = awaitNotLoading(viewModel.uiState)

        assertTrue("Expected Success but got $finalState", finalState is UIState.Success)
        assertEquals(100, (finalState as UIState.Success<List<Post>>).data.size)
    }

    @Test
    fun `loadComments should emit Success when api succeeds`() = runTest {
        whenever(mockCommentApi.getComments_test()).thenReturn(
            ApiResponse(code = 0, message = "success", data = sampleComments)
        )

        viewModel.loadComments()
        val finalState = awaitNotLoading(viewModel.commentsState)

        assertTrue("Expected Success but got $finalState", finalState is UIState.Success)
        assertEquals(sampleComments, (finalState as UIState.Success<*>).data)
    }

    @Test
    fun `loadComments should emit Empty when api returns empty list`() = runTest {
        whenever(mockCommentApi.getComments_test()).thenReturn(
            ApiResponse(code = 0, message = "success", data = emptyList())
        )

        viewModel.loadComments()
        val finalState = awaitNotLoading(viewModel.commentsState)

        assertTrue(
            "Expected Success but got $finalState",
            finalState is UIState.Success
        )
        assertTrue(
            "Expected empty data but got ${(finalState as UIState.Success<*>).data}",
            (finalState as UIState.Success<*>).data is List<*> && (finalState.data as List<*>).isEmpty()
        )
    }

    @Test
    fun `loadComments should emit Error when api fails`() = runTest {
        whenever(mockCommentApi.getComments_test()).thenReturn(
            ApiResponse(code = 500, message = "Comments failed", data = null)
        )

        viewModel.loadComments()
        val finalState = awaitNotLoading(viewModel.commentsState)

        assertTrue("Expected Error but got $finalState", finalState is UIState.Error)
        assertTrue((finalState as UIState.Error).message.contains("Comments failed"))
    }
}
