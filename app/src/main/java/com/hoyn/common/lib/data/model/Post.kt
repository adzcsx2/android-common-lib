package com.hoyn.common.lib.data.model

/**
 * 帖子数据模型
 *
 * 用于 UI 层展示的业务模型
 */
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
