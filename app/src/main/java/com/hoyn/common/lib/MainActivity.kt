package com.hoyn.common.lib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoyn.common.lib.databinding.ActivityMainBinding
import com.hoyn.common.lib.databinding.ItemDemoBinding
import com.hoyn.common.lib.demo.LogDemoActivity
import com.hoyn.common.lib.demo.MmkvDemoActivity
import com.hoyn.common.lib.demo.NetworkDemoActivity
import com.hoyn.common.lib.demo.StatusBarDemoActivity
import com.hoyn.common.lib.demo.ToastDemoActivity
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils

/**
 * Demo 列表入口
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Demo 列表
    private val demoList = listOf(
        DemoItem("Toast 示例", "展示各种 Toast 样式") { ToastDemoActivity.start(this) },
        DemoItem("网络请求示例", "展示 ViewModel + Repository + 网络请求（使用公开 API）") { NetworkDemoActivity.start(this) },
        DemoItem("MMKV 示例", "展示键值存储操作") { MmkvDemoActivity.start(this) },
        DemoItem("日志示例", "展示不同级别日志输出") { LogDemoActivity.start(this) },
        DemoItem("状态栏示例", "展示状态栏设置") { StatusBarDemoActivity.start(this) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
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
            binding.root.onClick { item.action.invoke() }
        }
    }
}
