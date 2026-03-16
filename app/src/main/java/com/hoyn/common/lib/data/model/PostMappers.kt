package com.hoyn.common.lib.data.model

import com.hoyn.common.lib.data.local.entity.PostEntity

fun Post.toEntity(cachedAt: Long): PostEntity {
    return PostEntity(
        userId = userId,
        id = id,
        title = title,
        body = body,
        cachedAt = cachedAt
    )
}

fun PostEntity.toDomain(): Post {
    return Post(
        userId = userId,
        id = id,
        title = title,
        body = body
    )
}