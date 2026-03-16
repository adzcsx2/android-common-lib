package com.hoyn.common.lib.ui.network

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.databinding.ItemPostBinding

/**
 * 帖子列表适配器
 *
 * 用于展示帖子列表数据
 */
class PostAdapter : RecyclerView.Adapter<PostViewHolder>() {

    private val items = mutableListOf<Post>()

    /**
     * 提交新数据
     */
    fun submitList(newItems: List<Post>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

/**
 * 帖子 ViewHolder
 */
class PostViewHolder(
    private val binding: ItemPostBinding
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * 绑定数据
     */
    fun bind(post: Post) {
        binding.tvTitle.text = post.title
        binding.tvBody.text = post.body
    }
}
