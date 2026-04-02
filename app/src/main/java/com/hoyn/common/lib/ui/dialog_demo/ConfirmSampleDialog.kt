package com.hoyn.common.lib.ui.dialog_demo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.hoyn.common.base.BaseDialogFragment
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.databinding.DialogConfirmSampleBinding
import com.hoyn.common.ui.ext.click

/**
 * 基于 BaseDialogFragment 的示例弹窗。
 */
class ConfirmSampleDialog : BaseDialogFragment<DialogConfirmSampleBinding, NoViewModel>() {

    /** 弹窗标题 */
    var title: CharSequence by argument("")
    /** 弹窗正文消息 */
    var message: CharSequence by argument("")

    companion object {
        /**
         * 通过实例 DSL 创建确认弹窗
         *
         * @param block DSL 配置块，用于设置弹窗参数
         * @return 配置好的 ConfirmSampleDialog 实例
         */
        fun create(block: ConfirmSampleDialog.() -> Unit): ConfirmSampleDialog {
            return ConfirmSampleDialog().apply(block)
        }
    }

    /**
     * 渲染标题和正文，并绑定按钮关闭事件。
     */
    override fun initView(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.text = title
        binding.tvMessage.text = message
        binding.btnCancel.click { dismissSafely() }
        binding.btnConfirm.click { dismissSafely() }
    }
}

/**
 * 以 DSL 方式展示确认弹窗，并应用默认窗口配置
 *
 * 默认配置：不可点击外部关闭、不可返回键取消、宽度 MATCH_PARENT
 *
 * @param fragmentManager FragmentManager 实例
 * @param dsl DSL 配置块，用于设置弹窗参数
 * @return 显示中的 ConfirmSampleDialog 实例
 */
fun initConfirmSampleDialog(
    fragmentManager: FragmentManager,
    dsl: ConfirmSampleDialog.() -> Unit
): ConfirmSampleDialog {
    return ConfirmSampleDialog.create(dsl).apply {
        touchOutside = false
        isCancelable = false
        mWidth = ViewGroup.LayoutParams.MATCH_PARENT
    }.also {
        it.showSafely(fragmentManager)
    }
}