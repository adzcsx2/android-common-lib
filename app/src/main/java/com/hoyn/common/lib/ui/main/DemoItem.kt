package com.hoyn.common.lib.ui.main

/**
 * Demo 列表项数据类
 *
 * @param title 标题
 * @param description 描述
 * @param badge 徽章文本（可选）
 * @param action 点击事件
 */
data class DemoItem(
    val title: String,
    val description: String,
    val badge: String? = null,
    val action: () -> Unit
)
