package com.hoyn.common.lib.ui.log_demo

import android.os.Bundle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.databinding.ActivityLogDemoBinding
import com.hoyn.common.ui.ext.click
import com.hoyn.common.ui.toast.ToastUtil
import com.hoyn.common.utils.Logger

/**
 * 日志示例页面
 *
 * 使用 BaseActivity 作为基类
 * 语言设置由 BaseActivity 统一处理
 *
 * TAG 由 BaseActivity 自动提供
 * 启动方式：context.startActivity<LogDemoActivity>()
 */
class LogDemoActivity : BaseActivity<ActivityLogDemoBinding, NoViewModel>() {

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
        binding.btnBack.click { finish() }


        binding.btnDebug.click {
            Logger.d("这是一条 DEBUG 日志")
            showToast("已输出 DEBUG 日志")
        }

        binding.btnInfo.click {
            Logger.i("这是一条 INFO 日志")
            showToast("已输出 INFO 日志")
        }

        binding.btnWarn.click {
            Logger.w("这是一条 WARN 日志")
            showToast("已输出 WARN 日志")
        }

        binding.btnError.click {
            Logger.e("这是一条 ERROR 日志")
            showToast("已输出 ERROR 日志")
        }

        binding.btnJson.click {
            val testData = mapOf("name" to "CommonLib", "version" to "1.0.0")
            Logger.json(testData)
            showToast("已输出 JSON 日志")
        }
    }

    /**
     * 显示 Toast 提示
     *
     * @param message 消息内容
     */
    private fun showToast(message: String) {
        ToastUtil.show(message)
    }
}
