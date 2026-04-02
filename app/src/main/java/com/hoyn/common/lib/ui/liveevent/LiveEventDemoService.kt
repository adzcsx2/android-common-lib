package com.hoyn.common.lib.ui.liveevent

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.Observer
import com.hoyn.common.base.event.GlobalLiveEvent
import com.hoyn.common.base.event.Message

/**
 * LiveEvent Demo 服务
 *
 * 后台监听 GlobalLiveEvent 消息，收到指定 Code 的消息后回复响应消息
 */
class LiveEventDemoService : Service() {

    /** 消息订阅观察者 */
    private var messageObserver: Observer<Message>? = null

    /**
     * 服务创建时订阅 GlobalLiveEvent 消息
     *
     * 过滤 [CODE_SERVICE_REQUEST] 消息并回复响应
     */
    override fun onCreate() {
        super.onCreate()
        messageObserver = GlobalLiveEvent.subscribeMessage { message ->
            if (message.code != CODE_SERVICE_REQUEST) {
                return@subscribeMessage
            }
            GlobalLiveEvent.sendMessage(
                CODE_SERVICE_RESPONSE,
                "Service收到LiveEvent消息: ${message.msg}"
            )
        }
        GlobalLiveEvent.sendMessage(CODE_SERVICE_RESPONSE, "Service已启动并开始监听LiveEvent")
    }

    /**
     * 处理启动命令
     *
     * @param intent 启动 Intent
     * @param flags 附加标志
     * @param startId 启动 ID
     * @return START_STICKY 表示系统杀掉后自动重启
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * 服务销毁时取消订阅并通知
     */
    override fun onDestroy() {
        messageObserver?.let(GlobalLiveEvent::removeMessageObserver)
        messageObserver = null
        GlobalLiveEvent.sendMessage(CODE_SERVICE_RESPONSE, "Service已停止并取消订阅")
        super.onDestroy()
    }

    /**
     * 绑定服务（不支持绑定）
     *
     * @param intent 绑定 Intent
     * @return null，不支持绑定
     */
    override fun onBind(intent: Intent?): IBinder? = null
}
