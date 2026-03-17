package com.hoyn.common.lib.ui.fragment_demo

import android.os.Bundle
import android.content.Context
import android.content.Intent
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityFragmentDemoBinding
import com.hoyn.common.ui.ext.onClick

class FragmentDemoActivity : BaseActivity<ActivityFragmentDemoBinding, NoViewModel>() {

    companion object {
        private const val EXTRA_INITIAL_SESSION_ID = "extra_initial_session_id"
        private const val EXTRA_INITIAL_COUNT = "extra_initial_count"

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