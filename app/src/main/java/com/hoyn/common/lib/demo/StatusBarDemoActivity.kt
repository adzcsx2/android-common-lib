package com.hoyn.common.lib.demo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hoyn.common.lib.databinding.ActivityStatusBarDemoBinding
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils
import com.hoyn.common.ui.utils.StatusBarHelper

/**
 * 状态栏示例页面
 */
class StatusBarDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatusBarDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBarDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.btnBack.onClick { finish() }

        // 默认状态
        binding.btnDefault.setOnClickListener {
            StatusBarHelper.translucent(this, Color.parseColor("#6200EE"))
            ToastUtils.show(this, "已设置为默认紫色")
        }

        // 红色状态栏
        binding.btnRed.setOnClickListener {
            StatusBarHelper.translucent(this, Color.RED)
            ToastUtils.show(this, "已设置为红色")
        }

        // 绿色状态栏
        binding.btnGreen.setOnClickListener {
            StatusBarHelper.translucent(this, Color.GREEN)
            ToastUtils.show(this, "已设置为绿色")
        }

        // 蓝色状态栏
        binding.btnBlue.setOnClickListener {
            StatusBarHelper.translucent(this, Color.BLUE)
            ToastUtils.show(this, "已设置为蓝色")
        }

        // 透明状态栏
        binding.btnTransparent.setOnClickListener {
            StatusBarHelper.translucent(this)
            ToastUtils.show(this, "已设置为透明")
        }

        // 浅色状态栏（深色图标）
        binding.btnLight.setOnClickListener {
            StatusBarHelper.translucent(this, Color.WHITE)
            ToastUtils.show(this, "已设置为浅色（深色图标）")
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, StatusBarDemoActivity::class.java))
        }
    }
}
