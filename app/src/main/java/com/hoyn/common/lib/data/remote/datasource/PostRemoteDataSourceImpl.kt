package com.hoyn.common.lib.data.remote.datasource

import com.hoyn.common.lib.data.remote.api.PostApi
import com.hoyn.common.lib.data.repository.PostLoadResult

/**
 * 帖子远程数据源实现
 *
 * 封装网络请求逻辑，处理网络异常
 *
 * @param api 网络接口
 */
class PostRemoteDataSourceImpl(
    private val api: PostApi
) : PostRemoteDataSource {

    /**
     * 从网络获取帖子列表
     *
     * 限制最多取前 10 条数据，并记录缓存时间戳
     *
     * @return Result 包含 PostLoadResult 或网络异常
     */
    override suspend fun getPosts(): Result<PostLoadResult> {
        return try {
            val posts = api.getPosts().take(10)
            val cachedAt = System.currentTimeMillis()
            Result.success(
                PostLoadResult(
                    posts = posts,
                    isFromCache = false,
                    updatedAt = cachedAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
