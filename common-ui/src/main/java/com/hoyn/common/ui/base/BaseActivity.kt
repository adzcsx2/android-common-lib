package com.hoyn.common.ui.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.hoyn.common.ui.toast.ToastUtils
import com.hoyn.common.utils.fixInputMethodManagerLeak
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * BaseActivity
 *
 * 提供通用的 Activity 基类功能
 * 支持协程、ViewBinding、触摸事件分发
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), CoroutineScope by MainScope() {

    protected lateinit var binding: VB

    // 保存 MyTouchListener 接口的列表
    private val myTouchListeners = mutableListOf<OnTouchListener>()

    /**
     * 记录是否需要跳转
     */
    private var startNavigation: Boolean = false

    fun getIsNavigate(): Boolean = startNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        onCreateBefore()
        super.onCreate(savedInstanceState)
        binding = createBinding()
        setContentView(binding.root)
        initView(savedInstanceState)
        initData()
    }

    override fun onResume() {
        if (startNavigation) {
            onRestartNavigate()
            startNavigation = false
        }
        super.onResume()
    }

    override fun onStop() {
        startNavigation = true
        super.onStop()
    }

    @Deprecated("Use onActivityResult APIs")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        dispatchFragmentResult(requestCode, resultCode, data)
    }

    private fun dispatchFragmentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            getFragmentListLast()?.let {
                if (it is IFragmentResult) {
                    it.onFragmentResult(requestCode, resultCode, data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 页面跳转后返回的回调
     */
    protected open fun onRestartNavigate() {}

    protected abstract fun createBinding(): VB

    protected open fun initView(savedInstanceState: Bundle?) {}

    protected open fun initData() {}

    /**
     * 这里可以做一些 setContentView 之前的操作
     * 如全屏、常亮、设置 Navigation 颜色、状态栏颜色等
     */
    protected open fun onCreateBefore() {
        // 取消严格模式 FileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
    }

    /**
     * 提供给 Fragment 注册自己的触摸事件的方法
     */
    fun registerTouchListener(listener: OnTouchListener) {
        myTouchListeners.add(listener)
    }

    /**
     * 提供给 Fragment 取消注册自己的触摸事件的方法
     */
    fun unregisterTouchListener(listener: OnTouchListener?) {
        myTouchListeners.remove(listener)
    }

    /**
     * 分发触摸事件给所有注册了 OnTouchListener 的接口
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        for (listener in myTouchListeners) {
            ev?.let { listener.onTouchEvent(it) }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        cancel()
        // 移除所有 Activity 类型的 Toast 防止内存泄漏
        ToastUtils.cancelActivityToast(this)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            fixInputMethodManagerLeak(this)
        }
        super.onDestroy()
    }

    /**
     * 获取 Fragment 列表中最后一个 Fragment
     */
    fun getFragmentListLast(): Fragment? {
        return try {
            supportFragmentManager.fragments.firstOrNull()
                ?.childFragmentManager?.fragments?.lastOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取 Fragment 列表
     */
    fun getFragmentLists(): List<Fragment> {
        return try {
            supportFragmentManager.fragments.firstOrNull()
                ?.childFragmentManager?.fragments ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 触摸事件监听接口
     */
    interface OnTouchListener {
        fun onTouchEvent(ev: MotionEvent)
    }

    /**
     * Fragment 结果回调接口
     */
    interface IFragmentResult {
        fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent?)
    }
}
