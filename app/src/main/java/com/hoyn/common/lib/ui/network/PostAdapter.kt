package com.hoyn.common.lib.ui.network

import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hoyn.common.lib.R
import com.hoyn.common.lib.data.model.Post

/**
 * 帖子列表适配器
 *
 * 用于展示帖子列表数据
 */
class PostAdapter : BaseQuickAdapter<Post, QuickViewHolder>() {

    /**
     * 创建 ViewHolder
     *
     * @param context 上下文
     * @param parent 父布局
     * @param viewType 视图类型
     * @return QuickViewHolder 实例
     */
    override fun onCreateViewHolder(
        context: android.content.Context,
        parent: android.view.ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_post, parent)
    }

    /**
     * 绑定数据到 ViewHolder
     *
     * @param holder ViewHolder 实例
     * @param position 位置索引
     * @param item 帖子数据，为 null 时跳过绑定
     */
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: Post?) {
        if (item == null) return
        holder.setText(R.id.tvTitle, item.title)
        holder.setText(R.id.tvBody, item.body)
    }
}
