package com.hoyn.common.lib.data.repository

import com.hoyn.common.lib.data.model.Post

/**
 * 帖子加载结果
 *
 * 封装帖子列表的加载结果，包含数据来源和更新时间信息
 *
 * @property posts 帖子列表
 * @property isFromCache 是否来自本地缓存
 * @property updatedAt 数据更新时间戳（毫秒）
 */
data class PostLoadResult(
    val posts: List<Post>,
    val isFromCache: Boolean,
    val updatedAt: Long
)