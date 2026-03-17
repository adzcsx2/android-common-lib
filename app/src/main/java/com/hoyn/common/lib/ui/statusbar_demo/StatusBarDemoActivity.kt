package com.hoyn.common.lib.ui.statusbar_demo

import android.graphics.Color
import android.os.Bundle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.databinding.ActivityStatusBarDemoBinding
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils
import com.hoyn.common.ui.utils.StatusBarHelper

/**
 * 状态栏示例页面
 *
 * 使用 BaseActivity 作为基类
 * 语言设置由 BaseActivity 统一处理
 *
 * TAG 由 BaseActivity 自动提供
 * 启动方式：context.startActivity<StatusBarDemoActivity>()
 */
class StatusBarDemoActivity : BaseActivity<ActivityStatusBarDemoBinding, NoViewModel>() {

    override fun initView(savedInstanceState: Bundle?) {
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
}
