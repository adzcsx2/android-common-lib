package com.hoyn.common.lib.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.lib.databinding.ActivityMmkvDemoBinding
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils
import com.hoyn.common.utils.MMKVUtils

/**
 * MMKV 示例页面
 *
 * 使用 BaseActivity 作为基类
 */
class MmkvDemoActivity : BaseActivity<ActivityMmkvDemoBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MmkvDemoActivity::class.java))
        }
    }

    override fun createBinding(): ActivityMmkvDemoBinding {
        return ActivityMmkvDemoBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        setupViews()
        loadSavedValue()
    }

    private fun setupViews() {
        binding.btnBack.onClick { finish() }

        // 保存
        binding.btnSaveString.setOnClickListener {
            val value = binding.etInput.text.toString()
            if (value.isNotBlank()) {
                MMKVUtils.put("demo_string", value)
                binding.tvResult.text = "已保存: $value"
                ToastUtils.show(this, "保存成功")
            } else {
                ToastUtils.show(this, "请输入内容")
            }
        }

        // 读取
        binding.btnLoadString.setOnClickListener {
            val value = MMKVUtils.getString("demo_string", "")
            if (value.isNotBlank()) {
                binding.tvResult.text = "读取结果: $value"
                ToastUtils.show(this, "读取成功")
            } else {
                binding.tvResult.text = "没有保存的数据"
            }
        }

        // 删除
        binding.btnDelete.setOnClickListener {
            MMKVUtils.remove("demo_string")
            binding.tvResult.text = "已删除"
            binding.etInput.setText("")
            ToastUtils.show(this, "已删除")
        }
    }

    private fun loadSavedValue() {
        val value = MMKVUtils.getString("demo_string", "")
        if (value.isNotBlank()) {
            binding.tvResult.text = "当前保存的值: $value"
        } else {
            binding.tvResult.text = "暂无保存数据"
        }
    }
}
