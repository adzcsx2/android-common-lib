package com.hoyn.common.lib.ui.mmkv_demo

import android.os.Bundle
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.databinding.ActivityMmkvDemoBinding
import com.hoyn.common.ui.ext.click
import com.hoyn.common.ui.toast.ToastUtil
import com.hoyn.common.utils.MMKVUtils

/**
 * MMKV 示例页面
 *
 * 使用 BaseActivity 作为基类
 * 语言设置由 BaseActivity 统一处理
 *
 * TAG 由 BaseActivity 自动提供
 * 启动方式：context.startActivity<MmkvDemoActivity>()
 */
class MmkvDemoActivity : BaseActivity<ActivityMmkvDemoBinding, NoViewModel>() {

    /**
     * 初始化视图
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun initView(savedInstanceState: Bundle?) {
        setupViews()
        loadSavedValue()
    }

    /**
     * 设置视图和点击事件
     */
    private fun setupViews() {
        binding.btnBack.click { finish() }

        // 保存
        binding.btnSaveString.click {
            val value = binding.etInput.text.toString()
            if (value.isNotBlank()) {
                MMKVUtils.put("demo_string", value)
                binding.tvResult.text = "已保存: $value"
                ToastUtil.show("保存成功")
            } else {
                ToastUtil.show("请输入内容")
            }
        }

        // 读取
        binding.btnLoadString.click {
            val value = MMKVUtils.getString("demo_string", "")
            if (value.isNotBlank()) {
                binding.tvResult.text = "读取结果: $value"
                ToastUtil.show("读取成功")
            } else {
                binding.tvResult.text = "没有保存的数据"
            }
        }

        // 删除
        binding.btnDelete.click {
            MMKVUtils.remove("demo_string")
            binding.tvResult.text = "已删除"
            binding.etInput.setText("")
            ToastUtil.show("已删除")
        }
    }

    /**
     * 加载已保存的值
     */
    private fun loadSavedValue() {
        val value = MMKVUtils.getString("demo_string", "")
        if (value.isNotBlank()) {
            binding.tvResult.text = "当前保存的值: $value"
        } else {
            binding.tvResult.text = "暂无保存数据"
        }
    }
}
