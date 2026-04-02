package com.hoyn.common.lib.data.model

/**
 * 帖子数据模型
 *
 * 对应 JSONPlaceholder API 返回的帖子数据结构，用于 UI 层展示的业务模型
 *
 * @property userId 发布帖子的用户 ID
 * @property id 帖子的唯一 ID
 * @property title 帖子标题
 * @property body 帖子正文内容
 */
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
