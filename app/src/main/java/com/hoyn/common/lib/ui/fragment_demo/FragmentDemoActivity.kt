package com.hoyn.common.lib.ui.fragment_demo

import android.os.Bundle
import android.content.Context
import android.content.Intent
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityFragmentDemoBinding
import com.hoyn.common.ui.ext.onClick

/**
 * Fragment 示例页面
 *
 * 展示 Fragment 的使用
 */
class FragmentDemoActivity : BaseActivity<ActivityFragmentDemoBinding, NoViewModel>() {

    companion object {
        private const val EXTRA_INITIAL_SESSION_ID = "extra_initial_session_id"
        private const val EXTRA_INITIAL_COUNT = "extra_initial_count"

        /**
         * 创建启动 FragmentDemoActivity 的 Intent
         *
         * @param context Context 实例
         * @param initialSessionId 初始会话 ID
         * @param initialCount 初始计数
         * @return 启动 Activity 的 Intent
         */
        fun createIntent(
            context: Context,
            initialSessionId: String? = null,
            initialCount: Int = 0
        ): Intent {
            return Intent(context, FragmentDemoActivity::class.java).apply {
                putExtra(EXTRA_INITIAL_COUNT, initialCount)
                if (initialSessionId != null) {
                    putExtra(EXTRA_INITIAL_SESSION_ID, initialSessionId)
                }
            }
        }
    }

    /**
     * 初始化视图
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun initView(savedInstanceState: Bundle?) {
        binding.btnBack.onClick { finish() }

        if (savedInstanceState == null) {
            val initialSessionId = intent.getStringExtra(EXTRA_INITIAL_SESSION_ID)
            val initialCount = intent.getIntExtra(EXTRA_INITIAL_COUNT, 0)
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    FragmentDemoFragment.newInstance(initialSessionId, initialCount)
                )
                .commitNow()
        }
    }
}