package com.hoyn.common.lib.data.repository

import com.hoyn.common.lib.data.local.datasource.PostLocalDataSource
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.model.toEntity
import com.hoyn.common.lib.data.remote.datasource.PostRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 帖子数据仓库
 *
 * 实现网络优先策略：
 * 1. 优先从网络获取数据
 * 2. 网络成功时写入本地缓存
 * 3. 网络失败时从本地缓存加载
 *
 * @param remoteDataSource 远程数据源
 * @param localDataSource 本地数据源
 * @param ioDispatcher IO 调度器
 */
class PostRepository(
    private val remoteDataSource: PostRemoteDataSource,
    private val localDataSource: PostLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * 获取帖子列表
     *
     * 策略：网络优先，失败时加载本地缓存
     *
     * @return Result 包含帖子列表或错误信息
     */
    suspend fun getPosts(): Result<PostLoadResult> = withContext(ioDispatcher) {
        val remoteResult = remoteDataSource.getPosts()

        if (remoteResult.isSuccess) {
            val result = remoteResult.getOrThrow()
            persistRemotePosts(result.posts, result.updatedAt)
            return@withContext remoteResult
        }

        localDataSource.getPosts()
    }

    /**
     * 清除本地缓存
     */
    suspend fun clearCache() = withContext(ioDispatcher) {
        localDataSource.clearPosts()
    }

    private suspend fun persistRemotePosts(posts: List<Post>, cachedAt: Long) {
        try {
            localDataSource.replacePosts(posts.map { post -> post.toEntity(cachedAt) }, cachedAt)
        } catch (error: Exception) {
            System.err.println("Failed to update local cache after a successful network fetch: ${error.message}")
        }
    }
}
