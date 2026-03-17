package com.hoyn.common.lib.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.hoyn.common.lib.data.local.db.AppDatabase
import com.hoyn.common.lib.data.local.entity.PostEntity
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.model.toDomain
import com.hoyn.common.lib.data.model.toEntity
import com.hoyn.common.lib.data.remote.api.PostApi
import com.hoyn.common.network.RetrofitFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 帖子数据仓库
 *
 * 实现网络优先策略：
 * 1. 优先从网络获取数据
 * 2. 网络成功时写入 Room 缓存
 * 3. 网络失败时从 Room 缓存加载
 *
 * @param localDataSource 本地数据源（Room）
 * @param api 网络接口
 * @param ioDispatcher IO 调度器
 */
class PostRepository internal constructor(
    private val localDataSource: PostLocalDataSource,
    private val api: PostApi,
    private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

        @Volatile
        private var INSTANCE: PostRepository? = null

        /**
         * 获取 PostRepository 单例实例
         *
         * @param context Context 实例
         * @return PostRepository 实例
         */
        fun getInstance(context: Context): PostRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PostRepository(
                    localDataSource = RoomPostLocalDataSource(
                        AppDatabase.getInstance(context.applicationContext)
                    ),
                    api = RetrofitFactory.createService(BASE_URL),
                    ioDispatcher = Dispatchers.IO
                ).also { INSTANCE = it }
            }
        }
    }

    /**
     * 获取帖子列表
     *
     * 策略：网络优先，失败时加载本地缓存
     *
     * @return Result 包含帖子列表或错误信息
     */
    suspend fun getPosts(): Result<PostLoadResult> = withContext(ioDispatcher) {
        val remotePosts = try {
            api.getPosts().take(10)
        } catch (error: Exception) {
            return@withContext loadCachedPostsAfterNetworkFailure(error)
        }

        val cachedAt = System.currentTimeMillis()
        persistRemotePosts(remotePosts, cachedAt)

        Result.success(
            PostLoadResult(
                posts = remotePosts,
                isFromCache = false,
                updatedAt = cachedAt
            )
        )
    }

    /**
     * 仅从本地获取帖子
     *
     * 不尝试网络请求，直接从本地缓存读取
     *
     * @return Result 包含帖子列表或错误信息
     */
    suspend fun getPostsFromLocal(): Result<PostLoadResult> = withContext(ioDispatcher) {
        loadCachedPostsOrFailure("无本地缓存")
    }

    /**
     * 清除本地缓存
     */
    suspend fun clearCache() = withContext(ioDispatcher) {
        localDataSource.clearPosts()
    }

    /**
     * 获取缓存数量
     *
     * @return 缓存的帖子数量
     */
    suspend fun getCacheCount(): Int = withContext(ioDispatcher) {
        localDataSource.getCount()
    }

    /**
     * 是否有缓存
     *
     * @return true 表示有缓存，false 表示没有
     */
    suspend fun hasCache(): Boolean = getCacheCount() > 0

    /**
     * 获取上次更新时间
     *
     * @return 最后一次缓存更新的时间戳（毫秒）
     */
    suspend fun getLastUpdateTime(): Long = withContext(ioDispatcher) {
        localDataSource.getLatestCacheTime() ?: 0L
    }

    private suspend fun persistRemotePosts(remotePosts: List<Post>, cachedAt: Long) {
        try {
            localDataSource.replacePosts(remotePosts.map { post -> post.toEntity(cachedAt) })
        } catch (error: Exception) {
            System.err.println("Failed to update Room cache after a successful network fetch: ${error.message}")
        }
    }

    private suspend fun loadCachedPostsAfterNetworkFailure(networkError: Exception): Result<PostLoadResult> {
        return try {
            loadCachedPostsOrFailure(resolveErrorMessage(networkError), networkError)
        } catch (storageError: Exception) {
            Result.failure(
                Exception(
                    "${resolveErrorMessage(networkError)}，且本地缓存读取失败",
                    storageError
                )
            )
        }
    }

    private suspend fun loadCachedPostsOrFailure(
        emptyCacheMessage: String,
        cause: Throwable? = null
    ): Result<PostLoadResult> {
        val cachedPosts = localDataSource.getAllPosts()
        if (cachedPosts.isEmpty()) {
            return Result.failure(Exception(emptyCacheMessage, cause))
        }

        return Result.success(cachedPosts.toCacheResult())
    }

    private fun resolveErrorMessage(error: Throwable): String {
        return when (error) {
            is UnknownHostException,
            is ConnectException,
            is SocketTimeoutException -> "网络不可用且无本地缓存"
            is HttpException -> "网络请求失败且无本地缓存"
            else -> error.message ?: "加载失败且无本地缓存"
        }
    }
}

internal interface PostLocalDataSource {
    suspend fun getAllPosts(): List<PostEntity>
    suspend fun replacePosts(posts: List<PostEntity>)
    suspend fun clearPosts()
    suspend fun getCount(): Int
    suspend fun getLatestCacheTime(): Long?
}

private class RoomPostLocalDataSource(
    private val database: AppDatabase
) : PostLocalDataSource {

    override suspend fun getAllPosts(): List<PostEntity> = database.postDao().getAllPosts()

    override suspend fun replacePosts(posts: List<PostEntity>) {
        database.withTransaction {
            database.postDao().clearPosts()
            database.postDao().insertPosts(posts)
        }
    }

    override suspend fun clearPosts() {
        database.postDao().clearPosts()
    }

    override suspend fun getCount(): Int = database.postDao().getCount()

    override suspend fun getLatestCacheTime(): Long? = database.postDao().getLatestCacheTime()
}

private fun List<PostEntity>.toCacheResult(): PostLoadResult {
    return PostLoadResult(
        posts = map { entity -> entity.toDomain() },
        isFromCache = true,
        updatedAt = maxOfOrNull { entity -> entity.cachedAt } ?: 0L
    )
}
