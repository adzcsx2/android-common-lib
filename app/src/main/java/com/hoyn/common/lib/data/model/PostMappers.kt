package com.hoyn.common.lib.data.model

import com.hoyn.common.lib.data.local.entity.PostEntity

/**
 * 将 Post 业务模型转换为 PostEntity 本地实体
 *
 * @param cachedAt 缓存时间戳（毫秒）
 * @return 转换后的 PostEntity 实体对象
 */
fun Post.toEntity(cachedAt: Long): PostEntity {
    return PostEntity(
        userId = userId,
        id = id,
        title = title,
        body = body,
        cachedAt = cachedAt
    )
}

/**
 * 将 PostEntity 本地实体转换为 Post 业务模型
 *
 * @return 转换后的 Post 业务模型对象
 */
fun PostEntity.toDomain(): Post {
    return Post(
        userId = userId,
        id = id,
        title = title,
        body = body
    )
}