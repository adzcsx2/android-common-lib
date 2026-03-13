package com.hoyn.common.lib.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hoyn.common.lib.databinding.ActivityMmkvDemoBinding
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.toast.ToastUtils
import com.hoyn.common.utils.MMKVUtils

/**
 * MMKV 示例页面
 */
class MmkvDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMmkvDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMmkvDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        loadSavedValue()
    }

    private fun setupViews() {
        binding.btnBack.onClick { finish() }

        // 保存字符串
        binding.btnSaveString.setOnClickListener {
            val value = binding.etInput.text.toString()
            if (value.isNotBlank()) {
                MMKVUtils.put("demo_string", value)
                ToastUtils.show(this, "已保存: $value")
                loadSavedValue()
            } else {
                ToastUtils.show(this, "请输入内容")
            }
        }

        // 读取字符串
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

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MmkvDemoActivity::class.java))
        }
    }
}
