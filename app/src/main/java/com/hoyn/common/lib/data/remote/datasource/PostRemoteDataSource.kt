package com.hoyn.common.lib.data.remote.datasource

import com.hoyn.common.lib.data.repository.PostLoadResult

/**
 * 帖子远程数据源接口
 *
 * 定义从网络获取数据的抽象
 */
interface PostRemoteDataSource {

    /**
     * 从网络获取帖子列表
     *
     * @return Result 包含 PostLoadResult 或网络错误
     */
    suspend fun getPosts(): Result<PostLoadResult>
}
