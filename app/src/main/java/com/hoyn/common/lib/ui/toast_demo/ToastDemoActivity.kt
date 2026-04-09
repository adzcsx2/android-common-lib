package com.hoyn.common.lib.ui.toast_demo

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import com.google.android.material.snackbar.Snackbar
import com.hjq.toast.style.BlackToastStyle
import com.hjq.toast.style.WhiteToastStyle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityToastDemoBinding
import com.hoyn.common.ui.ext.click
import com.hoyn.common.ui.ext.gone
import com.hoyn.common.ui.ext.visible

/**
 * Toast 示例页面
 *
 * 使用新的 ToastUtils API：
 * - 无需传入 Context
 * - 使用 Application Context 避免内存泄漏
 * - 对外统一使用 show*，内部封装 debugShow 便于调试定位
 * - 参考 Toaster 官方 MainActivity，覆盖常见策略、跨页面和样式场景
 *
 * TAG 由 BaseActivity 自动提供
 * 启动方式：context.startActivity<ToastDemoActivity>()
 */
class ToastDemoActivity : BaseActivity<ActivityToastDemoBinding, NoViewModel>() {

    private val pendingActions = mutableListOf<Runnable>()

    /**
     * 初始化视图
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun initView(savedInstanceState: Bundle?) {
        setupViews()
    }

    override fun onCleanUp() {
        pendingActions.forEach(binding.root::removeCallbacks)
        pendingActions.clear()
        toast.setImmediateStrategy()
    }

    /**
     * 设置视图和点击事件
     */
    private fun setupViews() {
        // 返回按钮，关闭当前页面
        binding.btnBack.click { finish() }
        // 隐藏队列提示文字，默认不可见
        binding.tvQueueHint.gone()

        // 显示默认 Toast（使用 Application Context，无内存泄漏风险）
        binding.btnShowToast.click {
            toast.show(R.string.toast_demo_message_short)
        }

        // 显示短时 Toast（SHORT 时长）
        binding.btnShortToast.click {
            toast.show(R.string.toast_demo_message_short_result)
        }

        // 显示长时 Toast（LONG 时长）
        binding.btnLongToast.click {
            toast.showLong(R.string.toast_demo_message_long)
        }

        // 显示跨页面 Toast（使用全局 Window，即使页面切换也不会消失）
        binding.btnCrossPageToast.click {
            toast.showGlobal(R.string.toast_demo_message_cross_page)
        }

        // 显示延迟 Toast（延迟 2 秒后展示）
        binding.btnDelayedToast.click {
            toast.delayedShow(R.string.toast_demo_message_delayed, 2000)
        }

        // 在子线程中显示 Toast（验证 ToastUtils 内部已切换到主线程）
        binding.btnThreadToast.click {
            Thread {
                toast.show(R.string.toast_demo_message_thread)
            }.start()
        }

        // 显示 Toast 后立即跳转到新页面并关闭当前页面（验证 Toast 不随页面销毁而消失）
        binding.btnStartActivityToast.click {
            toast.show(R.string.toast_demo_message_start_activity)
            startActivity(Intent(this, ToastDemoActivity::class.java))
            finish()
        }

        // 使用白色主题样式显示 Toast
        binding.btnWhiteStyle.click {
            toast.showWithStyle(
                R.string.toast_demo_message_white_style,
                WhiteToastStyle()
            )
        }

        // 使用黑色主题样式显示 Toast
        binding.btnBlackStyle.click {
            toast.showWithStyle(
                R.string.toast_demo_message_black_style,
                BlackToastStyle()
            )
        }

        // 使用 Info 信息样式布局显示 Toast（蓝色提示）
        binding.btnInfoStyle.click {
            toast.showWithLayout(
                R.string.toast_demo_message_info_style,
                R.layout.toast_info
            )
        }

        // 使用 Warn 警告样式布局显示 Toast（黄色提示）
        binding.btnWarnStyle.click {
            toast.showWithLayout(
                R.string.toast_demo_message_warn_style,
                R.layout.toast_warn
            )
        }

        // 使用 Success 成功样式布局显示 Toast（绿色提示）
        binding.btnSuccessStyle.click {
            toast.showWithLayout(
                R.string.toast_demo_message_success_style,
                R.layout.toast_success
            )
        }

        // 使用 Error 错误样式布局显示 Toast（红色提示）
        binding.btnErrorStyle.click {
            toast.showWithLayout(
                R.string.toast_demo_message_error_style,
                R.layout.toast_error
            )
        }

        // 使用自定义 XML 布局作为全局 Toast 视图，居中显示
        binding.btnCustomXmlToast.click {
            toast.showWithLayout(
                R.string.toast_demo_message_custom_xml,
                R.layout.toast_custom_view,
                Gravity.CENTER
            )
        }

        // 在屏幕居中位置显示短时 Toast
        binding.btnCenterToast.click {
            toast.showCenter(R.string.toast_demo_message_center)
        }

        // 在屏幕居中位置显示长时 Toast
        binding.btnCenterLongToast.click {
            toast.showLong(R.string.toast_demo_message_center_long) {
                gravity = Gravity.CENTER
                yOffset = 0
            }
        }

        // 切换到队列策略：新 Toast 会排队等候前一个显示完毕，同时显示提示文字
        binding.btnQueueStrategy.click {
            toast.setQueueStrategy()
            toast.show(R.string.toast_demo_message_queue_strategy)
            binding.tvQueueHint.visible()
        }

        // 点击提示文字后连续发送 3 条 Toast，验证队列策略下依次展示
        binding.tvQueueHint.click {
            repeat(3) { index ->
                toast.show(getString(R.string.toast_demo_message_queue, index + 1))
            }
        }

        // 切换到立即策略：新 Toast 会立即替换前一个，每 300ms 发送一条共 5 条验证替换效果
        binding.btnQueueTest.click {
            toast.setImmediateStrategy()
            repeat(5) { index ->
                postDemoAction((index * 300).toLong()) {
                    toast.show(getString(R.string.toast_demo_message_replace_previous, index + 1))
                }
            }
        }

        // 后台 Toast 演示：先提示用户即将回到桌面，2 秒后再提示，4 秒后回到桌面，5 秒后展示全局 Toast
        binding.btnBackgroundToast.click {
            // 立即显示 Snackbar 提示
            Snackbar.make(
                binding.root,
                R.string.toast_demo_message_background_hint,
                Snackbar.LENGTH_SHORT
            ).show()

            // 2 秒后显示第二个 Snackbar
            postDemoAction(2000) {
                Snackbar.make(
                    binding.root,
                    R.string.toast_demo_message_background_snackbar,
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            // 4 秒后回到桌面
            postDemoAction(4000) {
                startActivity(
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                    }
                )
            }

            // 5 秒后展示全局 Toast，验证即使 App 在后台也能正常弹出
            postDemoAction(5000) {
                toast.showGlobal(R.string.toast_demo_message_background_result)
            }
        }

        // 取消当前显示的 Toast，并恢复为立即策略
        binding.btnCancel.click {
            toast.cancel()
            toast.setImmediateStrategy()
        }
    }

    private fun postDemoAction(delayMillis: Long, action: () -> Unit) {
        val runnable = Runnable { action() }
        pendingActions += runnable
        binding.root.postDelayed(runnable, delayMillis)
    }
}
