package com.hoyn.common.lib.data.repository

import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.remote.api.PostApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 帖子数据仓库
 *
 * 实现网络优先策略：
 * 1. 优先从网络获取数据
 * 2. 网络成功时更新本地缓存
 * 3. 网络失败时从本地缓存加载
 *
 * 使用内存缓存实现（避免 KSP 兼容性问题）
 */
class PostRepository {

    companion object {
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    }

    private val api: PostApi by lazy {
        createApi()
    }

    // 内存缓存
    private var cachedPosts: List<Post> = emptyList()
    private var lastUpdateTime: Long = 0

    private fun createApi(): PostApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PostApi::class.java)
    }

    /**
     * 获取帖子列表
     *
     * 策略：网络优先，失败时加载本地缓存
     *
     * @return Result 包含帖子列表或错误信息
     */
    suspend fun getPosts(): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            // 1. 尝试从网络获取
            val remotePosts = api.getPosts().take(10)

            // 2. 保存到内存缓存
            cachedPosts = remotePosts
            lastUpdateTime = System.currentTimeMillis()

            Result.success(remotePosts)
        } catch (e: Exception) {
            // 3. 网络失败，尝试从缓存加载
            if (cachedPosts.isNotEmpty()) {
                Result.success(cachedPosts)
            } else {
                Result.failure(Exception("网络不可用且无本地缓存"))
            }
        }
    }

    /**
     * 仅从本地获取帖子
     */
    suspend fun getPostsFromLocal(): Result<List<Post>> = withContext(Dispatchers.IO) {
        if (cachedPosts.isNotEmpty()) {
            Result.success(cachedPosts)
        } else {
            Result.failure(Exception("无本地缓存"))
        }
    }

    /**
     * 清除本地缓存
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        cachedPosts = emptyList()
        lastUpdateTime = 0
    }

    /**
     * 获取缓存时间
     */
    fun getCacheTime(): Long = lastUpdateTime

    /**
     * 是否有缓存
     */
    fun hasCache(): Boolean = cachedPosts.isNotEmpty()
}
