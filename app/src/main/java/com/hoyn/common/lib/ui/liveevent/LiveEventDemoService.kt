package com.hoyn.common.lib.ui.liveevent

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.Observer
import com.hoyn.common.base.event.GlobalLiveEvent
import com.hoyn.common.core.Message

class LiveEventDemoService : Service() {

    private var messageObserver: Observer<Message>? = null

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        messageObserver?.let(GlobalLiveEvent::removeMessageObserver)
        messageObserver = null
        GlobalLiveEvent.sendMessage(CODE_SERVICE_RESPONSE, "Service已停止并取消订阅")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
