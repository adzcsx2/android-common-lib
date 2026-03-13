package com.hoyn.common.lib.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hoyn.common.lib.databinding.ActivityLogDemoBinding
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils
import com.hoyn.common.utils.Logger

/**
 * 日志示例页面
 */
class LogDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.btnWarn.setOnClickListener {
            Logger.w("这是一条 WARN 日志")
            showToast("已输出 WARN 日志")
        }

        binding.btnError.setOnClickListener {
            Logger.e("这是一条 ERROR 日志")
            showToast("已输出 ERROR 日志")
        }

        binding.btnJson.setOnClickListener {
            val json = """{"name": "CommonLib", "version": "1.0.0"}"""
            Logger.json(json)
            showToast("已输出 JSON 日志")
        }
    }

    private fun showToast(message: String) {
        ToastUtils.show(this, message)
        binding.tvResult.text = "最后操作: $message"
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LogDemoActivity::class.java))
        }
    }
}
