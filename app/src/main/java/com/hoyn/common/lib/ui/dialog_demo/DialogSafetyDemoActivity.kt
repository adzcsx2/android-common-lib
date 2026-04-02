package com.hoyn.common.lib.ui.dialog_demo

import android.os.Bundle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityDialogSafetyDemoBinding
import com.hoyn.common.ui.ext.click

/**
 * Dialog 安全使用示例。
 */
class DialogSafetyDemoActivity :
    BaseActivity<ActivityDialogSafetyDemoBinding, NoViewModel>() {

    /**
     * 绑定页面按钮事件，并初始化 Fragment 区域。
     */
    override fun initView(savedInstanceState: Bundle?) {
        binding.btnBack.click { finish() }
        binding.btnShowManagedDialog.click { showActivityDialog() }
        binding.btnShowReusableDialog.click { showReusableActivityDialog() }
        binding.btnShowDialogFragment.click {
            initConfirmSampleDialog(supportFragmentManager) {
                title = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_fragment_title)
                message = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_fragment_message)
            }
        }
        binding.btnShowParcelableDialog.click {
            showParcelableArgumentDialog()
        }
        binding.btnShowSerializableDialog.click {
            showSerializableArgumentDialog()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, DialogSafetyDemoFragment())
                .commitNow()
        }
    }

    /**
     * 展示一个由 Activity 发起的自定义 BaseDialogFragment。
     */
    private fun showActivityDialog() {
        initConfirmSampleDialog(supportFragmentManager) {
            title = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_plain_title)
            message = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_plain_message)
        }
    }

    /**
     * 展示一个带有全屏窗口配置的自定义 BaseDialogFragment。
     */
    private fun showReusableActivityDialog() {
        initFullscreenSampleDialog(supportFragmentManager) {
            title = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_reusable_title)
            message = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_reusable_message)
        }
    }

    /**
     * 展示 Parcelable 参数传递示例。
     */
    private fun showParcelableArgumentDialog() {
        initObjectArgumentDemoDialog(supportFragmentManager) {
            title = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_parcelable_title)
            parcelPayload = DemoParcelablePayload(
                orderId = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_parcelable_order_id),
                amount = 188,
                remark = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_parcelable_remark)
            )
        }
    }

    /**
     * 展示 Serializable 参数传递示例。
     */
    private fun showSerializableArgumentDialog() {
        initObjectArgumentDemoDialog(supportFragmentManager) {
            title = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_serializable_title)
            serialPayload = DemoSerializablePayload(
                owner = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_serializable_owner),
                enabled = true,
                scene = this@DialogSafetyDemoActivity.getString(R.string.dialog_demo_serializable_scene)
            )
        }
    }
}