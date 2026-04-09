package com.hoyn.common.lib.ui.statusbar_demo

import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityStatusBarDemoBinding
import com.hoyn.common.ui.ext.click
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

        // 默认状态
        binding.btnDefault.click {
            StatusBarHelper.setStatusBarColor(this, ContextCompat.getColor(this, R.color.purple_500), exitEdgeToEdge = true)
            toast.show("已设置为默认紫色")
        }

        // 红色状态栏
        binding.btnRed.click {
            StatusBarHelper.setStatusBarColor(this, Color.RED, exitEdgeToEdge = true)
            toast.show("已设置为红色")
        }

        // 绿色状态栏
        binding.btnGreen.click {
            StatusBarHelper.setStatusBarColor(this, Color.GREEN, exitEdgeToEdge = true)
            toast.show("已设置为绿色")
        }

        // 蓝色状态栏
        binding.btnBlue.click {
            StatusBarHelper.setStatusBarColor(this, Color.BLUE, exitEdgeToEdge = true)
            toast.show("已设置为蓝色")
        }

        // 透明状态栏
        binding.btnTransparent.click {
            StatusBarHelper.translucent(this)
            toast.show("已设置为透明")
        }

        // 浅色状态栏（深色图标）
        binding.btnLight.click {
            StatusBarHelper.setStatusBarColor(this, Color.WHITE, exitEdgeToEdge = true)
            toast.show("已设置为浅色（深色图标）")
        }
    }
}
