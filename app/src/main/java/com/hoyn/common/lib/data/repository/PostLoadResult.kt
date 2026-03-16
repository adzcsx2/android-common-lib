package com.hoyn.common.lib.data.repository

import com.hoyn.common.lib.data.model.Post

data class PostLoadResult(
    val posts: List<Post>,
    val isFromCache: Boolean,
    val updatedAt: Long
)