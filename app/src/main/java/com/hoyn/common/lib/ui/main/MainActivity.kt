package com.hoyn.common.lib.ui.main

import android.os.Bundle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityMainBinding
import com.hoyn.common.lib.ui.activity.LogDemoActivity
import com.hoyn.common.lib.ui.activity.MmkvDemoActivity
import com.hoyn.common.lib.ui.activity.StatusBarDemoActivity
import com.hoyn.common.lib.ui.activity.ToastDemoActivity
import com.hoyn.common.lib.ui.compose.ComposeDemoActivity
import com.hoyn.common.lib.ui.network.NetworkDemoActivity

/**
 * Demo 列表入口
 *
 * 使用 BaseActivity 作为基类
 * 语言设置由 BaseActivity 统一处理
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    // Demo 列表 - 使用字符串资源
    private val demoList by lazy {
        listOf(
            DemoItem(
                title = getString(R.string.compose_demo),
                description = getString(R.string.compose_demo_desc),
                badge = getString(R.string.recommended)
            ) {
                ComposeDemoActivity.start(this)
            },
            DemoItem(
                title = getString(R.string.toast_demo),
                description = getString(R.string.toast_demo_desc)
            ) {
                ToastDemoActivity.start(this)
            },
            DemoItem(
                title = getString(R.string.network_demo),
                description = getString(R.string.network_demo_desc)
            ) {
                NetworkDemoActivity.start(this)
            },
            DemoItem(
                title = getString(R.string.mmkv_demo),
                description = getString(R.string.mmkv_demo_desc)
            ) {
                MmkvDemoActivity.start(this)
            },
            DemoItem(
                title = getString(R.string.log_demo),
                description = getString(R.string.log_demo_desc)
            ) {
                LogDemoActivity.start(this)
            },
            DemoItem(
                title = getString(R.string.status_bar_demo),
                description = getString(R.string.status_bar_demo_desc)
            ) {
                StatusBarDemoActivity.start(this)
            }
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        setupRecyclerView()
    }

    override fun initData() {
        // 数据在 demoList 中已定义
    }

    private fun setupRecyclerView() {
        binding.rvDemo.adapter = DemoAdapter(demoList)
    }
}
