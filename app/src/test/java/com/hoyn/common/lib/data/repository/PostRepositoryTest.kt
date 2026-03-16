package com.hoyn.common.lib.data.repository

import com.hoyn.common.lib.data.local.entity.PostEntity
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.remote.api.PostApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class PostRepositoryTest {

    // ========== getPosts 成功场景 ==========

    @Test
    fun getPosts_returnsRemoteDataWhenNetworkSucceeds() = runBlocking {
        val remotePosts = listOf(
            Post(userId = 1, id = 1, title = "title", body = "body")
        )
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(),
            api = FakePostApi { remotePosts },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isSuccess)
        val loadResult = result.getOrThrow()
        assertEquals(remotePosts, loadResult.posts)
        assertFalse(loadResult.isFromCache)
    }

    @Test
    fun getPosts_returnsRemoteDataWhenCachePersistenceFails() = runBlocking {
        val remotePosts = listOf(
            Post(userId = 1, id = 1, title = "title", body = "body")
        )
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(replaceError = IllegalStateException("write failed")),
            api = FakePostApi { remotePosts },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isSuccess)
        val loadResult = result.getOrThrow()
        assertEquals(remotePosts, loadResult.posts)
        assertFalse(loadResult.isFromCache)
    }

    @Test
    fun getPosts_returnsEmptyListWhenNetworkReturnsEmpty() = runBlocking {
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(),
            api = FakePostApi { emptyList() },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isSuccess)
        val loadResult = result.getOrThrow()
        assertTrue(loadResult.posts.isEmpty())
        assertFalse(loadResult.isFromCache)
    }

    @Test
    fun getPosts_persistsRemoteDataToLocalCache() = runBlocking {
        val remotePosts = listOf(
            Post(userId = 1, id = 1, title = "title", body = "body")
        )
        val localDataSource = FakePostLocalDataSource()
        val repository = PostRepository(
            localDataSource = localDataSource,
            api = FakePostApi { remotePosts },
            ioDispatcher = Dispatchers.Unconfined
        )

        repository.getPosts()

        assertEquals(1, localDataSource.getCount())
    }

    // ========== getPosts 网络失败回退缓存场景 ==========

    @Test
    fun getPosts_returnsCacheWhenNetworkFails() = runBlocking {
        val cachedPosts = listOf(
            PostEntity(userId = 1, id = 1, title = "cached-1", body = "body-1", cachedAt = 10L),
            PostEntity(userId = 1, id = 2, title = "cached-2", body = "body-2", cachedAt = 20L)
        )
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = cachedPosts),
            api = FakePostApi { throw UnknownHostException("offline") },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isSuccess)
        val loadResult = result.getOrThrow()
        assertTrue(loadResult.isFromCache)
        assertEquals(20L, loadResult.updatedAt)
        assertEquals(listOf("cached-1", "cached-2"), loadResult.posts.map { post -> post.title })
    }

    @Test
    fun getPosts_returnsCacheOnSocketTimeout() = runBlocking {
        val cachedPosts = listOf(
            PostEntity(userId = 1, id = 1, title = "cached", body = "body", cachedAt = 100L)
        )
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = cachedPosts),
            api = FakePostApi { throw SocketTimeoutException("timeout") },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isFromCache)
    }

    @Test
    fun getPosts_returnsCacheOnConnectException() = runBlocking {
        val cachedPosts = listOf(
            PostEntity(userId = 1, id = 1, title = "cached", body = "body", cachedAt = 100L)
        )
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = cachedPosts),
            api = FakePostApi { throw ConnectException("connection refused") },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isFromCache)
    }

    // ========== getPosts 失败场景 ==========

    @Test
    fun getPosts_returnsFailureWhenNetworkFailsAndNoCache() = runBlocking {
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = emptyList()),
            api = FakePostApi { throw UnknownHostException("offline") },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("网络不可用") == true)
    }

    @Test
    fun getPosts_returnsFailureWhenCacheReadAlsoFails() = runBlocking {
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(readError = IllegalStateException("read failed")),
            api = FakePostApi { throw UnknownHostException("offline") },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isFailure)
        assertEquals("网络不可用且无本地缓存，且本地缓存读取失败", result.exceptionOrNull()?.message)
    }

    @Test
    fun getPosts_returnsFailureWithHttpErrorMessage() = runBlocking {
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = emptyList()),
            api = FakePostApi { throw MockHttpException() },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("网络请求失败") == true)
    }

    // ========== getPostsFromLocal 测试 ==========

    @Test
    fun getPostsFromLocal_returnsCachedPosts() = runBlocking {
        val cachedPosts = listOf(
            PostEntity(userId = 1, id = 1, title = "cached", body = "body", cachedAt = 100L)
        )
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = cachedPosts),
            api = FakePostApi { throw Exception("should not be called") },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPostsFromLocal()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isFromCache)
    }

    @Test
    fun getPostsFromLocal_returnsFailureWhenNoCache() = runBlocking {
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = emptyList()),
            api = FakePostApi { emptyList() },
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPostsFromLocal()

        assertTrue(result.isFailure)
        assertEquals("无本地缓存", result.exceptionOrNull()?.message)
    }

    // ========== clearCache 测试 ==========

    @Test
    fun clearCache_removesAllCachedPosts() = runBlocking {
        val cachedPosts = listOf(
            PostEntity(userId = 1, id = 1, title = "cached", body = "body", cachedAt = 100L)
        )
        val localDataSource = FakePostLocalDataSource(cachedPosts = cachedPosts)
        val repository = PostRepository(
            localDataSource = localDataSource,
            api = FakePostApi { emptyList() },
            ioDispatcher = Dispatchers.Unconfined
        )

        repository.clearCache()

        assertEquals(0, localDataSource.getCount())
    }

    // ========== getCacheCount 测试 ==========

    @Test
    fun getCacheCount_returnsCorrectCount() = runBlocking {
        val cachedPosts = listOf(
            PostEntity(userId = 1, id = 1, title = "1", body = "1", cachedAt = 100L),
            PostEntity(userId = 1, id = 2, title = "2", body = "2", cachedAt = 100L),
            PostEntity(userId = 1, id = 3, title = "3", body = "3", cachedAt = 100L)
        )
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = cachedPosts),
            api = FakePostApi { emptyList() },
            ioDispatcher = Dispatchers.Unconfined
        )

        val count = repository.getCacheCount()

        assertEquals(3, count)
    }

    @Test
    fun getCacheCount_returnsZeroWhenNoCache() = runBlocking {
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = emptyList()),
            api = FakePostApi { emptyList() },
            ioDispatcher = Dispatchers.Unconfined
        )

        val count = repository.getCacheCount()

        assertEquals(0, count)
    }

    // ========== hasCache 测试 ==========

    @Test
    fun hasCache_returnsTrueWhenCacheExists() = runBlocking {
        val cachedPosts = listOf(
            PostEntity(userId = 1, id = 1, title = "cached", body = "body", cachedAt = 100L)
        )
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = cachedPosts),
            api = FakePostApi { emptyList() },
            ioDispatcher = Dispatchers.Unconfined
        )

        assertTrue(repository.hasCache())
    }

    @Test
    fun hasCache_returnsFalseWhenNoCache() = runBlocking {
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = emptyList()),
            api = FakePostApi { emptyList() },
            ioDispatcher = Dispatchers.Unconfined
        )

        assertFalse(repository.hasCache())
    }

    // ========== getLastUpdateTime 测试 ==========

    @Test
    fun getLastUpdateTime_returnsLatestCacheTime() = runBlocking {
        val cachedPosts = listOf(
            PostEntity(userId = 1, id = 1, title = "1", body = "1", cachedAt = 100L),
            PostEntity(userId = 1, id = 2, title = "2", body = "2", cachedAt = 300L),
            PostEntity(userId = 1, id = 3, title = "3", body = "3", cachedAt = 200L)
        )
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = cachedPosts),
            api = FakePostApi { emptyList() },
            ioDispatcher = Dispatchers.Unconfined
        )

        val updateTime = repository.getLastUpdateTime()

        assertEquals(300L, updateTime)
    }

    @Test
    fun getLastUpdateTime_returnsZeroWhenNoCache() = runBlocking {
        val repository = PostRepository(
            localDataSource = FakePostLocalDataSource(cachedPosts = emptyList()),
            api = FakePostApi { emptyList() },
            ioDispatcher = Dispatchers.Unconfined
        )

        val updateTime = repository.getLastUpdateTime()

        assertEquals(0L, updateTime)
    }
}

private class FakePostApi(
    private val loader: suspend () -> List<Post>
) : PostApi {
    override suspend fun getPosts(): List<Post> = loader()
}

private class FakePostLocalDataSource(
    cachedPosts: List<PostEntity> = emptyList(),
    private val replaceError: Exception? = null,
    private val readError: Exception? = null
) : PostLocalDataSource {

    private var posts: List<PostEntity> = cachedPosts

    override suspend fun getAllPosts(): List<PostEntity> {
        readError?.let { throw it }
        return posts
    }

    override suspend fun replacePosts(posts: List<PostEntity>) {
        replaceError?.let { throw it }
        this.posts = posts
    }

    override suspend fun clearPosts() {
        posts = emptyList()
    }

    override suspend fun getCount(): Int = posts.size

    override suspend fun getLatestCacheTime(): Long? = posts.maxOfOrNull { post -> post.cachedAt }
}

/**
 * Mock HttpException for testing HTTP error scenarios
 */
private class MockHttpException : HttpException(
    retrofit2.Response.error<Any>(404, okhttp3.ResponseBody.create(null, "Not Found"))
)