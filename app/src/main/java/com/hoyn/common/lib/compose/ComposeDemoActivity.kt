package com.hoyn.common.lib.compose

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoyn.common.compose.base.BaseComposeActivity
import com.hoyn.common.compose.ext.collectAsUIState
import com.hoyn.common.compose.ext.getErrorOrNull
import com.hoyn.common.compose.ext.getDataOrNull
import com.hoyn.common.compose.ext.isError
import com.hoyn.common.compose.ext.isLoading
import com.hoyn.common.compose.theme.AppTheme
import com.hoyn.common.core.UIState
import com.hoyn.common.lib.demo.Post
import com.hoyn.common.lib.demo.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Compose 示例页面
 *
 * 展示完整的 Compose 架构示例：
 * - BaseComposeActivity -> ViewModel -> Repository -> Api -> UIState
 * - Material3 主题
 * - 列表展示
 */
class ComposeDemoActivity : BaseComposeActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ComposeDemoActivity::class.java))
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: ComposeDemoViewModel = viewModel()
        val uiState = viewModel.uiState.collectAsUIState()

        AppTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Compose 示例") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "返回"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // 功能按钮区
                    FeatureButtons(
                        onRefresh = { viewModel.loadPosts() },
                        onShowToast = { viewModel.showTestToast(this@ComposeDemoActivity) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 内容区
                    when {
                        uiState.isLoading -> {
                            LoadingContent()
                        }
                        uiState.isError -> {
                            ErrorContent(
                                message = uiState.getErrorOrNull() ?: "Unknown error",
                                onRetry = { viewModel.loadPosts() }
                            )
                        }
                        uiState.getDataOrNull()?.isEmpty() == true -> {
                            EmptyContent()
                        }
                        else -> {
                            PostList(posts = uiState.getDataOrNull() ?: emptyList())
                        }
                    }
                }
            }
        }
    }

    /**
     * 功能按钮
     */
    @Composable
    private fun FeatureButtons(
        onRefresh: () -> Unit,
        onShowToast: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRefresh,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("加载数据")
            }

            Button(
                onClick = onShowToast,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("测试 Toast")
            }
        }
    }

    /**
     * 加载中
     */
    @Composable
    private fun LoadingContent() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    /**
     * 错误页面
     */
    @Composable
    private fun ErrorContent(
        message: String,
        onRetry: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "加载失败",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }

    /**
     * 空页面
     */
    @Composable
    private fun EmptyContent() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    /**
     * 帖子列表
     */
    @Composable
    private fun PostList(posts: List<Post>) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(posts) { post ->
                PostItem(post = post)
            }
        }
    }

    /**
     * 帖子项
     */
    @Composable
    private fun PostItem(post: Post) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ID 徽章
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#${post.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = post.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Compose Demo ViewModel
 */
class ComposeDemoViewModel : androidx.lifecycle.ViewModel() {

    private val repository = PostRepository()

    private val _uiState = MutableStateFlow<UIState<List<Post>>>(UIState.Loading)
    val uiState: StateFlow<UIState<List<Post>>> = _uiState

    init {
        loadPosts()
    }

    fun loadPosts() {
        _uiState.value = UIState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getPosts()
            result.fold(
                onSuccess = { posts ->
                    _uiState.value = if (posts.isEmpty()) UIState.Empty else UIState.Success(posts)
                },
                onFailure = { error ->
                    _uiState.value = UIState.Error(-1, error.message ?: "Unknown error")
                }
            )
        }
    }

    fun showTestToast(context: android.content.Context) {
        com.hoyn.common.ui.toast.ToastUtils.show(context, "来自 Compose 的 Toast!")
    }
}
