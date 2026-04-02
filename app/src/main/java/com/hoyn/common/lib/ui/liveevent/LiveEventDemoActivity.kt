package com.hoyn.common.lib.ui.liveevent

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.base.event.GlobalLiveEvent
import com.hoyn.common.base.event.Message
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityLiveEventDemoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.hoyn.common.ui.ext.click

/**
 * LiveEvent Demo Activity
 *
 * 演示 GlobalLiveEvent 的各种使用方式：
 * - 普通消息发送与订阅
 * - 粘性消息发送与订阅
 * - 延迟消息发送
 * - Service 中使用 LiveEvent
 * - BroadcastReceiver 转发 LiveEvent 消息
 * - 手动订阅（Forever 模式）
 */
class LiveEventDemoActivity : BaseActivity<ActivityLiveEventDemoBinding, NoViewModel>() {

    /** 时间格式化器，精确到毫秒 */
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    /** 日志内容构建器 */
    private val logBuilder = StringBuilder()
    /** 普通消息订阅观察者 */
    private var normalObserver: Observer<Message>? = null
    /** 粘性消息订阅观察者 */
    private var stickyObserver: Observer<Message>? = null
    /** 手动订阅观察者（Forever 模式） */
    private var manualObserver: Observer<Message>? = null
    /** 组件生命周期观察者（接收 Service/Receiver 的响应） */
    private var componentObserver: Observer<Message>? = null
    /** Service 是否已启动 */
    private var serviceStarted = false

    /**
     * 初始化视图
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun initView(savedInstanceState: Bundle?) {
        setupClickListeners()
    }

    /**
     * 初始化数据
     */
    override fun initData() {
        componentObserver = GlobalLiveEvent.observeMessage(this) { msg ->
            when (msg.code) {
                CODE_SERVICE_RESPONSE -> log("Service Demo: ${msg.msg}")
                CODE_RECEIVER_RESPONSE -> log("Receiver Demo: ${msg.msg}")
            }
        }
        log("初始化完成，可以开始测试")
    }

    /**
     * 设置点击事件监听器
     */
    private fun setupClickListeners() {
        binding.btnSendNormal.click {
            val code = (100..999).random()
            val msg = "普通消息 #$code"
            GlobalLiveEvent.sendMessage(code, msg)
            log("发送普通消息: code=$code, msg=$msg")
        }

        binding.btnSendSticky.click {
            val code = (1000..1999).random()
            val msg = "粘性消息 #$code"
            GlobalLiveEvent.sendMessage(code, msg)
            log("发送粘性消息: code=$code, msg=$msg (订阅前发送，Sticky订阅可收到)")
        }

        binding.btnSendDelay.click {
            val code = (2000..2999).random()
            val msg = "延迟消息 #$code"
            GlobalLiveEvent.sendMessageDelay(code, msg, 2000)
            log("延迟2秒发送消息: code=$code, msg=$msg")
        }

        binding.btnToggleService.click {
            if (!serviceStarted) {
                startService(Intent(this, LiveEventDemoService::class.java))
                serviceStarted = true
                binding.btnToggleService.text = getString(R.string.stop_service_listener)
                log("已启动Service监听GlobalLiveEvent")
            } else {
                stopService(Intent(this, LiveEventDemoService::class.java))
                serviceStarted = false
                binding.btnToggleService.text = getString(R.string.start_service_listener)
                log("已停止Service监听GlobalLiveEvent")
            }
        }

        binding.btnSendToService.click {
            val msg = "来自Activity的Service消息 #${(3000..3999).random()}"
            GlobalLiveEvent.sendMessage(CODE_SERVICE_REQUEST, msg)
            log("发送给Service的LiveEvent消息: $msg")
        }

        binding.btnSendBroadcastDemo.click {
            val intent = Intent(this, LiveEventDemoReceiver::class.java).apply {
                action = ACTION_LIVE_EVENT_DEMO_BROADCAST
            }
            sendBroadcast(intent)
            log("已发送广播，等待Receiver转发到GlobalLiveEvent")
        }

        binding.btnSubscribeNormal.click {
            if (normalObserver == null) {
                normalObserver = GlobalLiveEvent.observeMessage(this) { msg ->
                    log("收到普通消息: code=${msg.code}, msg=${msg.msg}")
                }
                log("已订阅普通消息（再次点击可取消）")
                binding.btnSubscribeNormal.text = getString(R.string.unsubscribe_normal)
            } else {
                GlobalLiveEvent.removeMessageObserver(normalObserver!!)
                normalObserver = null
                log("已取消普通消息订阅")
                binding.btnSubscribeNormal.text = getString(R.string.subscribe_normal)
            }
        }

        binding.btnSubscribeSticky.click {
            if (stickyObserver == null) {
                stickyObserver = GlobalLiveEvent.observeStickyMessage(this) { msg ->
                    log("收到粘性消息: code=${msg.code}, msg=${msg.msg}")
                }
                log("已订阅粘性消息（再次点击可取消）")
                binding.btnSubscribeSticky.text = getString(R.string.unsubscribe_sticky)
            } else {
                GlobalLiveEvent.removeMessageObserver(stickyObserver!!)
                stickyObserver = null
                log("已取消粘性消息订阅")
                binding.btnSubscribeSticky.text = getString(R.string.subscribe_sticky)
            }
        }

        binding.btnSubscribeManual.click {
            if (manualObserver == null) {
                manualObserver = GlobalLiveEvent.createMessageObserver { msg ->
                    log("收到手动订阅消息: code=${msg.code}, msg=${msg.msg}")
                }
                GlobalLiveEvent.observeMessageForever(manualObserver!!)
                log("已手动订阅（Forever 模式）")
                binding.btnSubscribeManual.text = getString(R.string.cancel_manual_subscribe)
            } else {
                GlobalLiveEvent.removeMessageObserver(manualObserver!!)
                log("已取消手动订阅")
                manualObserver = null
                binding.btnSubscribeManual.text = getString(R.string.manual_subscribe)
            }
        }

        binding.btnClearLog.click {
            logBuilder.clear()
            binding.tvLog.text = ""
        }
    }

    /**
     * 清理资源
     */
    override fun onCleanUp() {
        if (serviceStarted) {
            stopService(Intent(this, LiveEventDemoService::class.java))
            serviceStarted = false
        }
        normalObserver?.let(GlobalLiveEvent::removeMessageObserver)
        stickyObserver?.let(GlobalLiveEvent::removeMessageObserver)
        manualObserver?.let(GlobalLiveEvent::removeMessageObserver)
        componentObserver?.let(GlobalLiveEvent::removeMessageObserver)
        normalObserver = null
        stickyObserver = null
        manualObserver = null
        componentObserver = null
        super.onCleanUp()
    }

    /**
     * 输出日志
     *
     * @param message 日志消息
     */
    private fun log(message: String) {
        val timestamp = timeFormat.format(Date())
        val logLine = "[$timestamp] $message\n"
        logBuilder.append(logLine)
        binding.tvLog.append(logLine)

        binding.scrollView.post {
            binding.scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }
}
