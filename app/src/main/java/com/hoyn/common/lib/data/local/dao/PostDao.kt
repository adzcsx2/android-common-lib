package com.hoyn.common.lib.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hoyn.common.lib.data.local.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY id ASC")
    suspend fun getAllPosts(): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("DELETE FROM posts")
    suspend fun clearPosts()

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getCount(): Int

    @Query("SELECT MAX(cachedAt) FROM posts")
    suspend fun getLatestCacheTime(): Long?
}