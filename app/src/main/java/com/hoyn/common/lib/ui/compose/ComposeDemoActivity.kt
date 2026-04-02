package com.hoyn.common.lib.ui.compose

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hoyn.common.compose.base.BaseComposeActivity
import com.hoyn.common.compose.ext.collectAsUIState
import com.hoyn.common.compose.ext.getErrorOrNull
import com.hoyn.common.compose.theme.AppTheme
import com.hoyn.common.core.UIState
import com.hoyn.common.lib.R
import com.hoyn.common.lib.data.model.Post

/**
 * Compose 示例页面
 *
 * 展示完整的 Compose 架构示例：
 * - BaseComposeActivity -> ViewModel -> Repository -> Api -> UIState
 * - 基类统一提供 AppTheme
 * - Material3 主题
 * - 列表展示
 * - 网络优先策略，离线缓存支持
 * - 语言设置由 BaseComposeActivity 统一处理
 * - ViewModel 由基类自动注入，无需在 Koin 中注册
 *
 * TAG 由 BaseActivity 自动提供
 * 启动方式：context.startActivity<ComposeDemoActivity>()
 */
class ComposeDemoActivity : BaseComposeActivity<ComposeDemoViewModel>() {

    @Composable
    override fun Content() {
        val uiState = viewModel.uiState.collectAsUIState()

        ComposeDemoScreen(
            uiState = uiState,
            onBack = { finish() },
            onRefresh = { viewModel.loadPosts() },
            onShowToast = { viewModel.showTestToast() }
        )
    }
}

/**
 * Compose Demo 主屏幕
 *
 * 包含顶部导航栏、功能按钮区域和根据状态切换的内容区域
 *
 * @param uiState 帖子列表的 UI 状态
 * @param onBack 返回按钮点击回调
 * @param onRefresh 刷新数据点击回调
 * @param onShowToast 测试 Toast 点击回调
 */
@Composable
fun ComposeDemoScreen(
    uiState: UIState<List<Post>>,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onShowToast: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.compose_demo_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
            FeatureButtons(
                onRefresh = onRefresh,
                onShowToast = onShowToast
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    LoadingContent()
                }

                uiState.isError -> {
                    ErrorContent(
                        message = uiState.getErrorOrNull()
                            ?: stringResource(R.string.unknown_error),
                        onRetry = onRefresh
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

/**
 * 功能按钮行
 *
 * 包含"加载数据"和"测试 Toast"两个按钮，各占一半宽度
 *
 * @param onRefresh 刷新数据点击回调
 * @param onShowToast 测试 Toast 点击回调
 */
@Composable
fun FeatureButtons(
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
            Text(stringResource(R.string.load_data))
        }

        Button(
            onClick = onShowToast,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.test_toast))
        }
    }
}

/**
 * 加载中内容组件
 *
 * 居中显示圆形进度指示器
 */
@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 错误内容组件
 *
 * 居中显示错误标题、错误消息和重试按钮
 *
 * @param message 错误消息文本
 * @param onRetry 重试按钮点击回调
 */
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.load_failed),
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
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * 空数据内容组件
 *
 * 居中显示心形图标和"暂无数据"提示
 */
@Composable
fun EmptyContent() {
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
            text = stringResource(R.string.no_data),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 帖子列表组件
 *
 * 使用 LazyColumn 垂直排列帖子卡片，每项间距 8dp
 *
 * @param posts 帖子数据列表
 */
@Composable
fun PostList(posts: List<Post>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts) { post ->
            PostItem(post = post)
        }
    }
}

/**
 * 帖子卡片项组件
 *
 * 显示帖子 ID 标签、标题和正文内容，使用 Material3 Card 样式
 *
 * @param post 帖子数据
 */
@Composable
fun PostItem(post: Post) {
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

@Preview(showBackground = true, name = "PostItem")
@Composable
private fun PreviewPostItem() {
    AppTheme {
        PostItem(
            post = Post(
                userId = 1,
                id = 1,
                title = "这是一个示例标题",
                body = "这是示例内容，用于展示帖子卡片的效果。"
            )
        )
    }
}

@Preview(showBackground = true, name = "Empty Content")
@Composable
private fun PreviewEmptyContent() {
    AppTheme {
        EmptyContent()
    }
}

@Preview(showBackground = true, name = "Loading Content")
@Composable
private fun PreviewLoadingContent() {
    AppTheme {
        LoadingContent()
    }
}

@Preview(showBackground = true, name = "Error Content")
@Composable
private fun PreviewErrorContent() {
    AppTheme {
        ErrorContent(
            message = "网络连接失败，请检查网络设置",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Feature Buttons")
@Composable
private fun PreviewFeatureButtons() {
    AppTheme {
        FeatureButtons(
            onRefresh = {},
            onShowToast = {}
        )
    }
}
