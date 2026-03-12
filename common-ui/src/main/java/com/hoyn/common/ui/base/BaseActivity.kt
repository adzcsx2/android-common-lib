package com.hoyn.common.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * BaseActivity
 *
 * 提供通用的 Activity 基类功能
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = createBinding()
        setContentView(binding.root)
        initView(savedInstanceState)
        initData()
    }

    protected abstract fun createBinding(): VB

    protected open fun initView(savedInstanceState: Bundle?) {}

    protected open fun initData() {}
}
