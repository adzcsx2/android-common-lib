package com.hoyn.common.lib.ui.log_demo

import android.os.Bundle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.databinding.ActivityLogDemoBinding
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils
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

    override fun initView(savedInstanceState: Bundle?) {
        setupViews()
    }

    private fun setupViews() {
        binding.btnBack.onClick { finish() }

        binding.btnVerbose.setOnClickListener {
            Logger.v("这是一条 VERBOSE 日志")
            showToast("已输出 VERBOSE 日志")
        }

        binding.btnDebug.setOnClickListener {
            Logger.d("这是一条 DEBUG 日志")
            showToast("已输出 DEBUG 日志")
        }

        binding.btnInfo.setOnClickListener {
            Logger.i("这是一条 INFO 日志")
            showToast("已输出 INFO 日志")
        }
    }

    private fun showToast(message: String) {
        ToastUtils.show(this, message)
    }
}
