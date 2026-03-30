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

    override suspend fun replacePosts(posts: List<PostEntity>, cachedAt: Long) {
        database.withTransaction {
            database.postDao().clearPosts()
            database.postDao().insertPosts(posts)
        }
    }

    override suspend fun clearPosts() {
        database.postDao().clearPosts()
    }

    override suspend fun getCount(): Int {
        return database.postDao().getCount()
    }

    private fun List<PostEntity>.toPostLoadResult(): PostLoadResult {
        return PostLoadResult(
            posts = map { entity -> entity.toDomain() },
            isFromCache = true,
            updatedAt = maxOfOrNull { entity -> entity.cachedAt } ?: 0L
        )
    }
}
