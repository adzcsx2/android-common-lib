package com.hoyn.common.lib.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoyn.common.lib.databinding.ActivityNetworkDemoBinding
import com.hoyn.common.lib.databinding.ItemPostBinding
import com.hoyn.common.network.RetrofitFactory
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import retrofit2.http.GET

/**
 * 网络请求示例页面
 * 使用 JSONPlaceholder 公开 API
 */
class NetworkDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNetworkDemoBinding
    private val viewModel: PostViewModel by lazy { PostViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNetworkDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeData()
        loadPosts()
    }

    private fun setupViews() {
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = PostAdapter()

        binding.btnRefresh.onClick {
            loadPosts()
        }

        binding.btnBack.onClick {
            finish()
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.posts.flowWithLifecycle(lifecycle).collect { posts ->
                (binding.rvPosts.adapter as PostAdapter).submitList(posts)
                binding.tvEmpty.visibility = if (posts.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.flowWithLifecycle(lifecycle).collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.error.flowWithLifecycle(lifecycle).collect { error ->
                if (error != null) {
                    binding.tvError.text = error
                    binding.tvError.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvError.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun loadPosts() {
        viewModel.loadPosts()
    }

    /**
     * 帖子适配器
     */
    private inner class PostAdapter : RecyclerView.Adapter<PostViewHolder>() {
        private val items = mutableListOf<Post>()

        fun submitList(newItems: List<Post>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
            val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PostViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }

    private inner class PostViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.tvTitle.text = post.title
            binding.tvBody.text = post.body
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, NetworkDemoActivity::class.java))
        }
    }
}

/**
 * 帖子数据类
 */
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

/**
 * 帖子 API 接口
 */
interface PostApi {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}

/**
 * 帖子 ViewModel（简化版，不使用 Repository）
 */
class PostViewModel : androidx.lifecycle.ViewModel() {

    private val api: PostApi by lazy {
        RetrofitFactory.createService("https://jsonplaceholder.typicode.com/", PostApi::class.java)
    }

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPosts() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val result = api.getPosts().take(10)
                _posts.value = result
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Unknown error"
            }
        }
    }
}
