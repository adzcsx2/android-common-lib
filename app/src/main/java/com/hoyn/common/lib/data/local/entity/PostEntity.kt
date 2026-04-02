package com.hoyn.common.lib.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 帖子本地数据库实体
 *
 * 对应 Room 数据库中的 posts 表，用于缓存帖子数据
 *
 * @property userId 发布帖子的用户 ID
 * @property id 帖子的唯一 ID（主键）
 * @property title 帖子标题
 * @property body 帖子正文内容
 * @property cachedAt 缓存时间戳（毫秒）
 */
@Entity(tableName = "posts")
data class PostEntity(
    val userId: Int,
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
    val cachedAt: Long
)