package com.hoyn.common.lib.data.repository

import com.hoyn.common.lib.data.local.datasource.PostLocalDataSource
import com.hoyn.common.lib.data.local.entity.PostEntity
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.remote.datasource.PostRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class PostRepositoryTest {

    // ========== getPosts 成功场景 ==========

    @Test
    fun getPosts_returnsRemoteDataWhenNetworkSucceeds() = runBlocking {
        val remoteResult = PostLoadResult(
            posts = listOf(Post(userId = 1, id = 1, title = "title", body = "body")),
            isFromCache = false,
            updatedAt = 1000L
        )
        val repository = PostRepository(
            remoteDataSource = FakePostRemoteDataSource { Result.success(remoteResult) },
            localDataSource = FakePostLocalDataSource(),
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isSuccess)
        val loadResult = result.getOrThrow()
        assertEquals(remoteResult.posts, loadResult.posts)
        assertFalse(loadResult.isFromCache)
    }

    @Test
    fun getPosts_returnsRemoteDataWhenCachePersistenceFails() = runBlocking {
        val remoteResult = PostLoadResult(
            posts = listOf(Post(userId = 1, id = 1, title = "title", body = "body")),
            isFromCache = false,
            updatedAt = 1000L
        )
        val repository = PostRepository(
            remoteDataSource = FakePostRemoteDataSource { Result.success(remoteResult) },
            localDataSource = FakePostLocalDataSource(replaceError = IllegalStateException("write failed")),
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isSuccess)
        val loadResult = result.getOrThrow()
        assertEquals(remoteResult.posts, loadResult.posts)
        assertFalse(loadResult.isFromCache)
    }

    @Test
    fun getPosts_returnsEmptyListWhenNetworkReturnsEmpty() = runBlocking {
        val remoteResult = PostLoadResult(
            posts = emptyList(),
            isFromCache = false,
            updatedAt = 1000L
        )
        val repository = PostRepository(
            remoteDataSource = FakePostRemoteDataSource { Result.success(remoteResult) },
            localDataSource = FakePostLocalDataSource(),
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
        val remoteResult = PostLoadResult(
            posts = listOf(Post(userId = 1, id = 1, title = "title", body = "body")),
            isFromCache = false,
            updatedAt = 1000L
        )
        val localDataSource = FakePostLocalDataSource()
        val repository = PostRepository(
            remoteDataSource = FakePostRemoteDataSource { Result.success(remoteResult) },
            localDataSource = localDataSource,
            ioDispatcher = Dispatchers.Unconfined
        )

        repository.getPosts()

        assertEquals(1, localDataSource.getCount())
    }

    // ========== getPosts 网络失败回退缓存场景 ==========

    @Test
    fun getPosts_returnsCacheWhenNetworkFails() = runBlocking {
        val cachedResult = PostLoadResult(
            posts = listOf(
                Post(userId = 1, id = 1, title = "cached-1", body = "body-1"),
                Post(userId = 1, id = 2, title = "cached-2", body = "body-2")
            ),
            isFromCache = true,
            updatedAt = 20L
        )
        val repository = PostRepository(
            remoteDataSource = FakePostRemoteDataSource { Result.failure(UnknownHostException("offline")) },
            localDataSource = FakePostLocalDataSource(cachedResult = cachedResult),
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isSuccess)
        val loadResult = result.getOrThrow()
        assertTrue(loadResult.isFromCache)
        assertEquals(20L, loadResult.updatedAt)
        assertEquals(listOf("cached-1", "cached-2"), loadResult.posts.map { post -> post.title })
    }

    // ========== getPosts 失败场景 ==========

    @Test
    fun getPosts_returnsFailureWhenNetworkFailsAndNoCache() = runBlocking {
        val repository = PostRepository(
            remoteDataSource = FakePostRemoteDataSource { Result.failure(UnknownHostException("offline")) },
            localDataSource = FakePostLocalDataSource(cachedResult = null),
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.getPosts()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("无本地缓存") == true)
    }

    // ========== clearCache 测试 ==========

    @Test
    fun clearCache_removesAllCachedPosts() = runBlocking {
        val cachedResult = PostLoadResult(
            posts = listOf(Post(userId = 1, id = 1, title = "cached", body = "body")),
            isFromCache = true,
            updatedAt = 100L
        )
        val localDataSource = FakePostLocalDataSource(cachedResult = cachedResult)
        val repository = PostRepository(
            remoteDataSource = FakePostRemoteDataSource { Result.success(PostLoadResult(emptyList(), false, 0L)) },
            localDataSource = localDataSource,
            ioDispatcher = Dispatchers.Unconfined
        )

        repository.clearCache()

        assertEquals(0, localDataSource.getCount())
    }
}

private class FakePostRemoteDataSource(
    private val loader: suspend () -> Result<PostLoadResult>
) : PostRemoteDataSource {
    override suspend fun getPosts(): Result<PostLoadResult> = loader()
}

private class FakePostLocalDataSource(
    private val cachedResult: PostLoadResult? = null,
    private val replaceError: Exception? = null
) : PostLocalDataSource {

    private var posts: List<PostEntity> = cachedResult?.posts?.map { post ->
        PostEntity(post.userId, post.id, post.title, post.body, cachedResult.updatedAt)
    } ?: emptyList()

    override suspend fun getPosts(): Result<PostLoadResult> {
        return if (posts.isEmpty()) {
            Result.failure(Exception("无本地缓存"))
        } else {
            Result.success(cachedResult!!)
        }
    }

    override suspend fun replacePosts(posts: List<PostEntity>, cachedAt: Long) {
        replaceError?.let { throw it }
        this.posts = posts
    }

    override suspend fun clearPosts() {
        posts = emptyList()
    }

    override suspend fun getCount(): Int = posts.size
}
