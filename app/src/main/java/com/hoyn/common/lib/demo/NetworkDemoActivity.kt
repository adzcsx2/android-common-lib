package com.hoyn.common.lib.demo

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.BaseViewModel
import com.hoyn.common.base.createViewModel
import com.hoyn.common.base.ext.observeAllUIEvents
import com.hoyn.common.core.UIState
import com.hoyn.common.lib.databinding.ActivityNetworkDemoBinding
import com.hoyn.common.lib.databinding.ItemPostBinding
import com.hoyn.common.network.BaseRepository
import com.hoyn.common.network.RetrofitFactory
import com.hoyn.common.network.exception.ExceptionHandle
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.GET

/**
 * 网络请求示例页面
 *
 * 展示完整架构链路：
 * - BaseActivity -> BaseViewModel -> BaseRepository -> Api -> UIState -> Event
 */
class NetworkDemoActivity : BaseActivity<ActivityNetworkDemoBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, NetworkDemoActivity::class.java))
        }
    }

    private val viewModel: PostViewModel by lazy { createViewModel { PostViewModel() } }

    override fun createBinding(): ActivityNetworkDemoBinding {
        return ActivityNetworkDemoBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        setupViews()
        observeData()
    }

    override fun initData() {
        viewModel.loadPosts()
    }

    private fun setupViews() {
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = PostAdapter()

        binding.btnRefresh.onClick {
            viewModel.loadPosts()
        }

        binding.btnBack.onClick {
            finish()
        }
    }

    private fun observeData() {
        // 观察 UI 状态
        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle)
                .collect { state -> renderState(state) }
        }

        // 观察 UI 事件
        observeAllUIEvents(
            viewModel = viewModel,
            onToast = { ToastUtils.show(this, it) },
            onShowDialog = { showLoading(it) },
            onDismissDialog = { hideLoading() }
        )
    }

    private fun renderState(state: UIState<List<Post>>) {
        when (state) {
            is UIState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvPosts.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
                binding.tvError.visibility = View.GONE
            }
            is UIState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.rvPosts.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
                binding.tvError.visibility = View.GONE
                (binding.rvPosts.adapter as PostAdapter).submitList(state.data)
            }
            is UIState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.rvPosts.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = state.message
            }
            is UIState.Empty -> {
                binding.progressBar.visibility = View.GONE
                binding.rvPosts.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvError.visibility = View.GONE
            }
        }
    }

    private fun showLoading(message: String?) {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
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
 * 帖子 Repository
 */
class PostRepository : BaseRepository<PostApi>() {

    companion object {
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    }

    override val api: PostApi by lazy {
        createApi(BASE_URL, PostApi::class.java)
    }

    /**
     * 获取帖子列表
     */
    suspend fun getPosts(): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val posts = api.getPosts().take(10)
            Result.success(posts.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 帖子 ViewModel
 *
 * 继承 BaseViewModel，展示完整架构模式
 */
class PostViewModel : BaseViewModel<PostRepository>() {

    override val repository: PostRepository by lazy { PostRepository() }

    private val _uiState = MutableStateFlow<UIState<List<Post>>>(UIState.Loading)
    val uiState: StateFlow<UIState<List<Post>>> = _uiState

    override fun handleException(throwable: Throwable): com.hoyn.common.core.ThrowableBean {
        val handled = ExceptionHandle.handleException(throwable)
        return com.hoyn.common.core.ThrowableBean(handled.code, handled.errMsg)
    }

    /**
     * 加载帖子列表
     */
    fun loadPosts() {
        _uiState.value = UIState.Loading

        launchUI {
            val result = repository.getPosts()
            result.fold(
                onSuccess = { posts ->
                    if (posts.isEmpty()) {
                        _uiState.value = UIState.Empty
                    } else {
                        _uiState.value = UIState.Success(posts)
                    }
                },
                onFailure = { error ->
                    _uiState.value = UIState.Error(-1, error.message ?: "Unknown error")
                    showToast(error.message ?: "Unknown error")
                }
            )
        }
    }
}
