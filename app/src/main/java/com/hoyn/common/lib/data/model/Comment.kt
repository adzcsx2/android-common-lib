package com.hoyn.common.lib.data.model

/**
 * 评论数据模型
 *
 * 对应 JSONPlaceholder API 返回的评论数据结构
 *
 * @property postId 所属帖子的 ID
 * @property id 评论的唯一 ID
 * @property name 评论标题/名称
 * @property email 评论者邮箱
 * @property body 评论正文内容
 */
data class Comment(
    val postId: Int,
    val id: Int,
    val name: String,
    val email: String,
    val body: String
)