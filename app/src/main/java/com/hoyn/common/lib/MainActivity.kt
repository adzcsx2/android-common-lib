package com.hoyn.common.lib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hoyn.common.lib.databinding.ActivityMainBinding
import com.hoyn.common.log.Logger
import com.hoyn.common.ui.ext.onClick
import com.hoyn.common.ui.ext.setVisible

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化日志
        Logger.init(BuildConfig.DEBUG, "DemoApp")

        // 演示日志功能
        Logger.d("MainActivity onCreate")

        // 演示扩展函数
        binding.btnTest.onClick {
            Logger.d("Button clicked!")
            binding.tvResult.setVisible(true)
            binding.tvResult.text = "Hello from CommonLib!"
        }
    }
}
