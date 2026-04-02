package com.hoyn.common.lib.ui.dialog_demo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.hoyn.common.base.BaseDialogFragment
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.DialogConfirmSampleBinding
import com.hoyn.common.ui.ext.click

/**
 * 展示 BaseDialogFragment 对 Parcelable 和 Serializable 参数的支持。
 */
class ObjectArgumentDemoDialog : BaseDialogFragment<DialogConfirmSampleBinding, NoViewModel>() {

    /** 弹窗标题 */
    var title: CharSequence by argument("")
    /** Parcelable 参数（订单数据） */
    var parcelPayload: DemoParcelablePayload? by argumentParcelable()
    /** Serializable 参数（功能配置） */
    var serialPayload: DemoSerializablePayload? by argumentSerializable()

    companion object {
        /**
         * 通过实例 DSL 创建对象参数演示弹窗
         *
         * @param block DSL 配置块，用于设置弹窗参数
         * @return 配置好的 ObjectArgumentDemoDialog 实例
         */
        fun create(block: ObjectArgumentDemoDialog.() -> Unit): ObjectArgumentDemoDialog {
            return ObjectArgumentDemoDialog().apply(block)
        }
    }

    /**
     * 将对象参数渲染成可见文本，便于直接观察传值结果。
     */
    override fun initView(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.text = title
        binding.tvMessage.text = buildDisplayMessage()
        binding.btnCancel.click { dismissSafely() }
        binding.btnConfirm.click { dismissSafely() }
    }

    /**
     * 组合 Parcelable 与 Serializable 的展示文案。
     */
    private fun buildDisplayMessage(): CharSequence {
        val lines = mutableListOf<String>()
        parcelPayload?.let { payload ->
            lines += getString(
                R.string.dialog_demo_parcelable_format,
                payload.orderId,
                payload.amount,
                payload.remark
            )
        }
        serialPayload?.let { payload ->
            lines += getString(
                R.string.dialog_demo_serializable_format,
                payload.owner,
                payload.enabled.toString(),
                payload.scene
            )
        }
        return lines.joinToString(separator = "\n")
    }
}

/**
 * 以 DSL 方式展示对象参数演示弹窗，并应用默认窗口配置
 *
 * 默认配置：不可点击外部关闭、不可返回键取消、宽度 MATCH_PARENT
 *
 * @param fragmentManager FragmentManager 实例
 * @param dsl DSL 配置块，用于设置弹窗参数
 * @return 显示中的 ObjectArgumentDemoDialog 实例
 */
fun initObjectArgumentDemoDialog(
    fragmentManager: FragmentManager,
    dsl: ObjectArgumentDemoDialog.() -> Unit
): ObjectArgumentDemoDialog {
    return ObjectArgumentDemoDialog.create(dsl).apply {
        touchOutside = false
        isCancelable = false
        mWidth = ViewGroup.LayoutParams.MATCH_PARENT
    }.also {
        it.showSafely(fragmentManager)
    }
}