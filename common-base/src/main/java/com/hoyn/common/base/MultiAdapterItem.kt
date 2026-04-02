package com.hoyn.common.base

/**
 * 多类型列表项包装器，配合 BRVAH [com.chad.library.adapter4.BaseMultiItemAdapter] 使用。
 *
 * 每个 [MultiAdapterItem] 代表一组与特定 [viewType] 关联的数据元素。
 * 对于普通列表，[data] 通常为单元素列表。
 *
 * @param T 该项组中数据元素的类型
 * @property data 与该项关联的数据元素列表
 * @property viewType 由适配器用来选择布局的视图类型标识
 */
data class MultiAdapterItem<T>(
    val data: List<T>,
    val viewType: Int
)
