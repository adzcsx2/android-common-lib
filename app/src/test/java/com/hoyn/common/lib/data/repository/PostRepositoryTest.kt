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
import java.net.UnknownHostException

class PostRepositoryTest {

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