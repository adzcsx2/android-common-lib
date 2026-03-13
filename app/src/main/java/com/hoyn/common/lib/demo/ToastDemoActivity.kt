package com.hoyn.common.lib.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hoyn.common.lib.databinding.ActivityToastDemoBinding
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils

/**
 * Toast 示例页面
 */
class ToastDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityToastDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToastDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        // 短 Toast
        binding.btnShortToast.onClick {
            ToastUtils.show(this, "这是一个短 Toast")
        }

        // 长 Toast
        binding.btnLongToast.onClick {
            ToastUtils.show(this, "这是一个长 Toast， 显示时间较长", 2000)
        }

        // 中间 Toast
        binding.btnCenterToast.onClick {
            ToastUtils.showCenterToast(this, "这是一个中间 Toast")
        }

        // 返回
        binding.btnBack.onClick {
            finish()
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ToastDemoActivity::class.java))
        }
    }
}
