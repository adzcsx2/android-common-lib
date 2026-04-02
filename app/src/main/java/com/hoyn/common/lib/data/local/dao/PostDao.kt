package com.hoyn.common.lib.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hoyn.common.lib.data.local.entity.PostEntity

/**
 * 帖子数据访问对象
 *
 * 提供 posts 表的增删查操作
 */
@Dao
interface PostDao {

    /**
     * 获取所有帖子，按 ID 升序排列
     *
     * @return 帖子实体列表
     */
    @Query("SELECT * FROM posts ORDER BY id ASC")
    suspend fun getAllPosts(): List<PostEntity>

    /**
     * 批量插入帖子，遇到主键冲突时替换已有记录
     *
     * @param posts 待插入的帖子实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    /**
     * 清空所有帖子记录
     */
    @Query("DELETE FROM posts")
    suspend fun clearPosts()

    /**
     * 获取帖子总数
     *
     * @return 帖子数量
     */
    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getCount(): Int

    /**
     * 获取最新的缓存时间戳
     *
     * @return 最新的缓存时间（毫秒），无数据时返回 null
     */
    @Query("SELECT MAX(cachedAt) FROM posts")
    suspend fun getLatestCacheTime(): Long?
}