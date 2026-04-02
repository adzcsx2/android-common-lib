package com.hoyn.common.lib.data.local.datasource

import androidx.room.withTransaction
import com.hoyn.common.lib.data.local.db.AppDatabase
import com.hoyn.common.lib.data.local.entity.PostEntity
import com.hoyn.common.lib.data.model.toDomain
import com.hoyn.common.lib.data.repository.PostLoadResult

/**
 * 帖子本地数据源实现
 *
 * 基于 Room 数据库的本地存储实现
 *
 * @param database Room 数据库实例
 */
class PostLocalDataSourceImpl(
    private val database: AppDatabase
) : PostLocalDataSource {

    /**
     * 获取缓存的帖子列表
     *
     * 若无本地缓存数据则返回 failure
     *
     * @return Result 包含 PostLoadResult 或错误信息
     */
    override suspend fun getPosts(): Result<PostLoadResult> {
        return try {
            val cachedPosts = database.postDao().getAllPosts()
            if (cachedPosts.isEmpty()) {
                Result.failure(Exception("无本地缓存"))
            } else {
                Result.success(cachedPosts.toPostLoadResult())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 替换所有帖子（先清空再插入）
     *
     * 在数据库事务中执行，保证原子性
     *
     * @param posts 新的帖子实体列表
     * @param cachedAt 缓存时间戳
     */
    override suspend fun replacePosts(posts: List<PostEntity>, cachedAt: Long) {
        database.withTransaction {
            database.postDao().clearPosts()
            database.postDao().insertPosts(posts)
        }
    }

    /**
     * 清除所有缓存帖子
     */
    override suspend fun clearPosts() {
        database.postDao().clearPosts()
    }

    /**
     * 获取缓存帖子数量
     *
     * @return 缓存的帖子数量
     */
    override suspend fun getCount(): Int {
        return database.postDao().getCount()
    }

    /**
     * 将 PostEntity 列表转换为 PostLoadResult
     *
     * 标记数据来源于缓存，并取最新的缓存时间戳
     *
     * @return PostLoadResult 包含转换后的帖子列表和缓存标记
     */
    private fun List<PostEntity>.toPostLoadResult(): PostLoadResult {
        return PostLoadResult(
            posts = map { entity -> entity.toDomain() },
            isFromCache = true,
            updatedAt = maxOfOrNull { entity -> entity.cachedAt } ?: 0L
        )
    }
}
