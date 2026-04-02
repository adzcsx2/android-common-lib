package com.hoyn.common.lib.ui.stack_demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hoyn.common.base.ActivityStackManager
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityStackManagerDemoBinding
import com.hoyn.common.lib.ui.dialog_demo.initConfirmSampleDialog
import com.hoyn.common.ui.ext.click
import com.hoyn.common.utils.LanguageHelper

/**
 * 非 BaseActivity 的 ActivityStackManager 接入示例。
 */
class StackManagerDemoActivity : AppCompatActivity() {

    companion object {
        /**
         * 创建启动 StackManagerDemoActivity 的 Intent
         *
         * @param context Context 实例
         * @return 启动 Activity 的 Intent
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, StackManagerDemoActivity::class.java)
        }
    }

    /** 视图绑定对象 */
    private lateinit var binding: ActivityStackManagerDemoBinding

    /**
     * 应用语言设置
     *
     * @param newBase 新的 Context
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 注册到 Activity 栈管理器
        ActivityStackManager.registerActivity(this)
        super.onCreate(savedInstanceState)
        binding = ActivityStackManagerDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        renderStackInfo()
    }

    override fun onResume() {
        super.onResume()
        // 恢复时刷新栈信息
        renderStackInfo()
    }

    override fun onDestroy() {
        // 从 Activity 栈管理器注销
        ActivityStackManager.unregisterActivity(this)
        super.onDestroy()
    }

    /**
     * 初始化视图和按钮事件
     */
    private fun initView() {
        binding.btnBack.click { finish() }
        binding.btnOpenNext.click {
            startActivity(createIntent(this))
        }
        binding.btnRefresh.click {
            renderStackInfo()
        }
        binding.btnShowDialog.click {
            showSampleDialog()
        }
        binding.btnFinishWithDialog.click {
            // 展示弹窗后立即 finish，测试弹窗生命周期安全性
            showSampleDialog()
            finish()
        }
    }

    /**
     * 渲染 Activity 栈信息到视图
     *
     * 显示当前栈顶 Activity、栈大小和完整的栈快照列表
     */
    private fun renderStackInfo() {
        val snapshot = ActivityStackManager.getActivityStackSnapshot()
        binding.tvCurrentValue.text = ActivityStackManager.current()?.javaClass?.simpleName
            ?: getString(R.string.stack_demo_empty)
        binding.tvCountValue.text = snapshot.size.toString()
        binding.tvSnapshotValue.text = if (snapshot.isEmpty()) {
            getString(R.string.stack_demo_empty)
        } else {
            snapshot.mapIndexed { index, activity ->
                "${index + 1}. ${activity.javaClass.simpleName}@${activity.hashCode().toString(16)}"
            }.joinToString(separator = "\n")
        }
    }

    /**
     * 展示示例确认弹窗
     */
    private fun showSampleDialog() {
        initConfirmSampleDialog(supportFragmentManager) {
            title = this@StackManagerDemoActivity.getString(R.string.stack_demo_dialog_title)
            message = this@StackManagerDemoActivity.getString(R.string.stack_demo_dialog_message)
        }
    }
}