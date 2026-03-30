package com.hoyn.common.lib.data.local.datasource

import com.hoyn.common.lib.data.local.entity.PostEntity
import com.hoyn.common.lib.data.repository.PostLoadResult

/**
 * 帖子本地数据源接口
 *
 * 定义本地数据存储的抽象
 */
interface PostLocalDataSource {

    /**
     * 获取缓存的帖子列表
     *
     * @return Result 包含 PostLoadResult 或错误（无缓存时返回 failure）
     */
    suspend fun getPosts(): Result<PostLoadResult>

    /**
     * 替换所有帖子（先清空再插入）
     *
     * @param posts 新的帖子列表
     * @param cachedAt 缓存时间戳
     */
    suspend fun replacePosts(posts: List<PostEntity>, cachedAt: Long)

    /**
     * 清除所有缓存
     */
    suspend fun clearPosts()

    /**
     * 获取缓存数量
     *
     * @return 缓存的帖子数量
     */
    suspend fun getCount(): Int
}
