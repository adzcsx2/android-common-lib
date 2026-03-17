package com.hoyn.common.lib.ui.toast_demo

import android.os.Bundle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.databinding.ActivityToastDemoBinding
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtil

/**
 * Toast 示例页面
 *
 * 使用新的 ToastUtil API：
 * - 无需传入 Context
 * - 使用 Application Context 避免内存泄漏
 * - 新 Toast 会取消前一个 Toast
 * - 语言设置由 BaseActivity 统一处理
 *
 * TAG 由 BaseActivity 自动提供
 * 启动方式：context.startActivity<ToastDemoActivity>()
 */
class ToastDemoActivity : BaseActivity<ActivityToastDemoBinding, NoViewModel>() {

    /**
     * 初始化视图
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun initView(savedInstanceState: Bundle?) {
        setupViews()
    }

    /**
     * 设置视图和点击事件
     */
    private fun setupViews() {
        // 返回
        binding.btnBack.onClick { finish() }

        // 短 Toast - 无需 Context
        binding.btnShortToast.onClick {
            ToastUtil.show("这是一个短 Toast")
        }

        // 长 Toast
        binding.btnLongToast.onClick {
            ToastUtil.showLong("这是一个长 Toast，显示时间较长")
        }

        // 中间 Toast
        binding.btnCenterToast.onClick {
            ToastUtil.showCenter("这是一个中间 Toast")
        }

        // 中间长 Toast
        binding.btnCenterLongToast.onClick {
            ToastUtil.showCenterLong("这是一个中间长 Toast")
        }

        // 队列优先级测试 - 快速连续显示多个 Toast
        binding.btnQueueTest.onClick {
            // 模拟快速连续点击，新 Toast 会取消前一个
            var count = 0
            repeat(5) {
                binding.root.postDelayed({
                    count++
                    ToastUtil.show("Toast #$count - 新的会取消前一个")
                }, (it * 300).toLong())
            }
        }

        // 取消当前 Toast
        binding.btnCancel.onClick {
            ToastUtil.cancel()
        }
    }
}
