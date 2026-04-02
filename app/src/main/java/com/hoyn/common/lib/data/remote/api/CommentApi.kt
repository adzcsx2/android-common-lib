package com.hoyn.common.lib.data.remote.api

import com.hoyn.common.lib.data.model.Comment
import com.hoyn.common.network.BaseResponse
import retrofit2.http.GET

/**
 * 评论接口
 */
interface CommentApi {

    /**
     * 获取评论列表
     *
     * @return 评论列表
     */
    @GET("comments")
    suspend fun getComments(): List<Comment>

    @GET("comments_test")
    suspend fun getComments_test(): BaseResponse<List<Comment>>
}