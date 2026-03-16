package com.hoyn.common.lib.data.remote.api

import com.hoyn.common.lib.data.model.Post
import retrofit2.http.GET

/**
 * 帖子 API 接口
 *
 * 定义网络请求接口
 */
interface PostApi {

    /**
     * 获取帖子列表
     */
    @GET("posts")
    suspend fun getPosts(): List<Post>
}
