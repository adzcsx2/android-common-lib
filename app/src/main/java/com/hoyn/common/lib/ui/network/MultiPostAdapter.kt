package com.hoyn.common.lib.ui.network

import android.content.Context
import android.view.ViewGroup
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hoyn.common.base.MultiAdapterItem
import com.hoyn.common.lib.R
import com.hoyn.common.lib.data.model.Comment

/**
 * 评论多类型列表适配器
 *
 * 支持两种布局样式：精选样式和紧凑样式
 */
class MultiPostAdapter : BaseMultiItemAdapter<MultiAdapterItem<Comment>>() {

    init {
        // 注册精选样式布局，标题显示评论名称
        addItemType(
            VIEW_TYPE_FEATURED,
            object : OnMultiItemAdapterListener<MultiAdapterItem<Comment>, QuickViewHolder> {
                override fun onCreate(
                    context: Context,
                    parent: ViewGroup,
                    viewType: Int
                ): QuickViewHolder {
                    return QuickViewHolder(R.layout.item_post_featured, parent)
                }

                override fun onBind(
                    holder: QuickViewHolder,
                    position: Int,
                    item: MultiAdapterItem<Comment>?
                ) {
                    if (item == null || item.data.isEmpty()) return
                    val comment = item.data.first()
                    holder.setText(R.id.tvTitle, comment.name)
                    holder.setText(R.id.tvBody, comment.body)
                }
            })

        // 注册紧凑样式布局，标题显示评论邮箱
        addItemType(
            VIEW_TYPE_COMPACT,
            object : OnMultiItemAdapterListener<MultiAdapterItem<Comment>, QuickViewHolder> {
                override fun onCreate(
                    context: Context,
                    parent: ViewGroup,
                    viewType: Int
                ): QuickViewHolder {
                    return QuickViewHolder(R.layout.item_post_compact, parent)
                }

                override fun onBind(
                    holder: QuickViewHolder,
                    position: Int,
                    item: MultiAdapterItem<Comment>?
                ) {
                    if (item == null || item.data.isEmpty()) return
                    val comment = item.data.first()
                    holder.setText(R.id.tvTitle, comment.email)
                    holder.setText(R.id.tvBody, comment.body)
                }
            })

        // 根据数据项中的 viewType 字段决定使用哪种布局
        onItemViewType { position, list ->
            list[position].viewType
        }
    }

    companion object {
        /** 精选样式视图类型 */
        const val VIEW_TYPE_FEATURED = 1
        /** 紧凑样式视图类型 */
        const val VIEW_TYPE_COMPACT = 2
    }
}
