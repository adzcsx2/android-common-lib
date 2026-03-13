package com.hoyn.common.compose.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment

/**
 * Base Compose Fragment
 *
 * 提供 Compose 在 Fragment 中使用的基础配置
 * 支持混用 Compose 和传统 View
 */
abstract class BaseComposeFragment : Fragment() {

    protected val composeView: ComposeView by lazy {
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return composeView
    }
}
