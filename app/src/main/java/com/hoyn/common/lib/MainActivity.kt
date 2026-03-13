package com.hoyn.common.lib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.lib.databinding.ActivityMainBinding
import com.hoyn.common.lib.databinding.ItemDemoBinding
import com.hoyn.common.lib.compose.ComposeDemoActivity
import com.hoyn.common.lib.demo.LogDemoActivity
import com.hoyn.common.lib.demo.MmkvDemoActivity
import com.hoyn.common.lib.demo.NetworkDemoActivity
import com.hoyn.common.lib.demo.StatusBarDemoActivity
import com.hoyn.common.lib.demo.ToastDemoActivity
import com.hoyn.common.ui.ext.onClick

/**
 * Demo 列表入口
 *
 * 使用 BaseActivity 作为基类
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    // Demo 列表
    private val demoList = listOf(
        DemoItem("Compose 示例", "Jetpack Compose 完整示例", "推荐") {
            ComposeDemoActivity.start(this)
        },
        DemoItem("Toast 示例", "展示各种 Toast 样式") {
            ToastDemoActivity.start(this)
        },
        DemoItem("网络请求示例", "展示 ViewModel + Repository + 网络请求") {
            NetworkDemoActivity.start(this)
        },
        DemoItem("MMKV 示例", "展示键值存储操作") {
            MmkvDemoActivity.start(this)
        },
        DemoItem("日志示例", "展示不同级别日志输出") {
            LogDemoActivity.start(this)
        },
        DemoItem("状态栏示例", "展示状态栏设置") {
            StatusBarDemoActivity.start(this)
        }
    )

    override fun initView(savedInstanceState: Bundle?) {
        setupRecyclerView()
    }

    override fun initData() {
        // 数据在 demoList 中已定义
    }

    private fun setupRecyclerView() {
        binding.rvDemo.layoutManager = LinearLayoutManager(this)
        binding.rvDemo.adapter = DemoAdapter(demoList)
    }

    /**
     * Demo 项数据类
     */
    data class DemoItem(
        val title: String,
        val description: String,
        val badge: String? = null,
        val action: () -> Unit
    )

    /**
     * Demo 列表适配器
     */
    private inner class DemoAdapter(
        private val items: List<DemoItem>
    ) : RecyclerView.Adapter<DemoViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoViewHolder {
            val binding = ItemDemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DemoViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DemoViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }

    /**
     * Demo 列表 ViewHolder
     */
    private inner class DemoViewHolder(
        private val binding: ItemDemoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DemoItem) {
            binding.tvTitle.text = item.title
            binding.tvDesc.text = item.description

            // 显示徽章
            if (item.badge != null) {
                binding.tvBadge.visibility = View.VISIBLE
                binding.tvBadge.text = item.badge
            } else {
                binding.tvBadge.visibility = View.GONE
            }

            binding.root.onClick { item.action.invoke() }
        }
    }
}
