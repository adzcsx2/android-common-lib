package com.hoyn.common.lib.ui.dialog_demo

import android.os.Bundle
import android.view.View
import com.hoyn.common.base.BaseFragment
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.FragmentDialogSafetyDemoBinding
import com.hoyn.common.ui.ext.click

/**
 * 展示 BaseFragment 中自定义 BaseDialogFragment 的使用方式。
 */
class DialogSafetyDemoFragment :
    BaseFragment<FragmentDialogSafetyDemoBinding, NoViewModel>() {

    /**
     * 绑定 Fragment 内的 dialog 展示按钮
     *
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态
     */
    override fun initView(view: View, savedInstanceState: Bundle?) {
        binding.btnShowManagedDialog.click {
            showFragmentDialog()
        }
        binding.btnShowAndRemove.click {
            showFragmentDialog()
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }
    }

    /**
     * 通过 childFragmentManager 展示自定义 dialog，使其跟随当前 Fragment 生命周期。
     */
    private fun showFragmentDialog() {
        initConfirmSampleDialog(childFragmentManager) {
            title = this@DialogSafetyDemoFragment.getString(R.string.dialog_demo_fragment_managed_title)
            message = this@DialogSafetyDemoFragment.getString(R.string.dialog_demo_fragment_managed_message)
        }
    }
}