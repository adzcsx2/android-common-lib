package com.hoyn.common.lib.ui.main

import android.os.Bundle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.base.startActivity
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityMainBinding
import com.hoyn.common.lib.ui.compose.ComposeDemoActivity
import com.hoyn.common.lib.ui.fragment_demo.FragmentDemoActivity
import com.hoyn.common.lib.ui.log_demo.LogDemoActivity
import com.hoyn.common.lib.ui.mmkv_demo.MmkvDemoActivity
import com.hoyn.common.lib.ui.network.NetworkDemoActivity
import com.hoyn.common.lib.ui.statusbar_demo.StatusBarDemoActivity
import com.hoyn.common.lib.ui.toast_demo.ToastDemoActivity
import com.hoyn.common.lib.ui.liveevent.LiveEventDemoActivity

/**
 * Demo 列表入口
 *
 * 使用 BaseActivity 作为基类
 * 语言设置由 BaseActivity 统一处理
 * 使用扩展函数启动其他 Activity
 */
class MainActivity : BaseActivity<ActivityMainBinding, NoViewModel>() {

    // Demo 列表 - 使用字符串资源
    private val demoList by lazy {
        listOf(
            DemoItem(
                title = getString(R.string.compose_demo),
                description = getString(R.string.compose_demo_desc),
                badge = getString(R.string.recommended)
            ) {
                startActivity<ComposeDemoActivity>()
            },
            DemoItem(
                title = getString(R.string.toast_demo),
                description = getString(R.string.toast_demo_desc)
            ) {
                startActivity<ToastDemoActivity>()
            },
            DemoItem(
                title = getString(R.string.network_demo),
                description = getString(R.string.network_demo_desc)
            ) {
                startActivity<NetworkDemoActivity>()
            },
            DemoItem(
                title = getString(R.string.fragment_demo),
                description = getString(R.string.fragment_demo_desc),
                badge = getString(R.string.recommended)
            ) {
                startActivity<FragmentDemoActivity>()
            },
            DemoItem(
                title = getString(R.string.mmkv_demo),
                description = getString(R.string.mmkv_demo_desc)
            ) {
                startActivity<MmkvDemoActivity>()
            },
            DemoItem(
                title = getString(R.string.log_demo),
                description = getString(R.string.log_demo_desc)
            ) {
                startActivity<LogDemoActivity>()
            },
            DemoItem(
                title = getString(R.string.status_bar_demo),
                description = getString(R.string.status_bar_demo_desc)
            ) {
                startActivity<StatusBarDemoActivity>()
            },
            DemoItem(
                title = getString(R.string.liveevent_demo),
                description = getString(R.string.liveevent_demo_desc)
            ) {
                startActivity<LiveEventDemoActivity>()
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
