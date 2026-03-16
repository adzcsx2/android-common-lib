package com.hoyn.common.lib.ui.log_demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.lib.databinding.ActivityLogDemoBinding
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils
import com.hoyn.common.utils.Logger

/**
 * 日志示例页面
 *
 * 使用 BaseActivity 作为基类
 * 语言设置由 BaseActivity 统一处理
 */
class LogDemoActivity : BaseActivity<ActivityLogDemoBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LogDemoActivity::class.java))
        }
    }

    override fun createBinding(): ActivityLogDemoBinding {
        return ActivityLogDemoBinding.inflate(layoutInflater)
    }

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
