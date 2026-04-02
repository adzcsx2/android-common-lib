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
 * 展示自定义窗口配置的全屏示例弹窗。
 */
class FullscreenSampleDialog : BaseDialogFragment<DialogConfirmSampleBinding, NoViewModel>() {

    /** 弹窗标题 */
    var title: CharSequence by argument("")
    /** 弹窗正文消息 */
    var message: CharSequence by argument("")

    companion object {
        /**
         * 通过实例 DSL 创建全屏示例弹窗
         *
         * @param block DSL 配置块，用于设置弹窗参数
         * @return 配置好的 FullscreenSampleDialog 实例
         */
        fun create(block: FullscreenSampleDialog.() -> Unit): FullscreenSampleDialog {
            return FullscreenSampleDialog().apply(block)
        }
    }

    /**
     * 渲染标题、内容和关闭按钮。
     */
    override fun initView(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.text = title
        binding.tvMessage.text = message
        binding.btnCancel.click { dismissSafely() }
        binding.btnConfirm.click { dismissSafely() }
    }
}

/**
 * 以 DSL 方式展示全屏示例弹窗，并应用满屏窗口配置
 *
 * 默认配置：不可点击外部关闭、不可返回键取消、宽高 MATCH_PARENT
 *
 * @param fragmentManager FragmentManager 实例
 * @param dsl DSL 配置块，用于设置弹窗参数
 * @return 显示中的 FullscreenSampleDialog 实例
 */
fun initFullscreenSampleDialog(
    fragmentManager: FragmentManager,
    dsl: FullscreenSampleDialog.() -> Unit
): FullscreenSampleDialog {
    return FullscreenSampleDialog.create(dsl).apply {
        touchOutside = false
        isCancelable = false
        mWidth = ViewGroup.LayoutParams.MATCH_PARENT
        mHeight = ViewGroup.LayoutParams.MATCH_PARENT
    }.also {
        it.showSafely(fragmentManager)
    }
}