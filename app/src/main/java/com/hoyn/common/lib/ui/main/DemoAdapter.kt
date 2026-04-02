package com.hoyn.common.lib.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hoyn.common.lib.databinding.ItemDemoBinding
import com.hoyn.common.ui.ext.click

/**
 * Demo 列表适配器
 *
 * 用于展示 Demo 列表
 */
class DemoAdapter(
    /** Demo 列表数据源 */
    private val items: List<DemoItem>
) : RecyclerView.Adapter<DemoViewHolder>() {

    /**
     * 创建 ViewHolder
     *
     * @param parent 父布局
     * @param viewType 视图类型
     * @return DemoViewHolder 实例
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoViewHolder {
        val binding = ItemDemoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DemoViewHolder(binding)
    }

    /**
     * 绑定数据到 ViewHolder
     *
     * @param holder ViewHolder 实例
     * @param position 位置索引
     */
    override fun onBindViewHolder(holder: DemoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    /**
     * 获取列表项数量
     *
     * @return 列表项总数
     */
    override fun getItemCount(): Int = items.size
}

/**
 * Demo 列表 ViewHolder
 */
class DemoViewHolder(
    private val binding: ItemDemoBinding
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * 绑定数据到视图
     *
     * 设置标题、描述和可选徽章，并绑定点击事件
     *
     * @param item Demo 列表项数据
     */
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

        binding.root.click { item.action.invoke() }
    }
}
