package com.hoyn.common.lib.ui.liveevent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hoyn.common.base.event.GlobalLiveEvent

/**
 * LiveEvent Demo 广播接收器
 *
 * 接收指定 Action 的广播，并将消息转发到 GlobalLiveEvent
 */
class LiveEventDemoReceiver : BroadcastReceiver() {

    /**
     * 接收广播并转发到 GlobalLiveEvent
     *
     * 仅处理 [ACTION_LIVE_EVENT_DEMO_BROADCAST]，其他 Action 忽略
     *
     * @param context 上下文
     * @param intent 广播 Intent
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_LIVE_EVENT_DEMO_BROADCAST) {
            return
        }
        GlobalLiveEvent.sendMessage(CODE_RECEIVER_RESPONSE, "BroadcastReceiver收到广播并发送LiveEvent消息")
    }
}
