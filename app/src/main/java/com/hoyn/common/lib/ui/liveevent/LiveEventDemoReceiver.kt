package com.hoyn.common.lib.ui.liveevent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hoyn.common.base.event.GlobalLiveEvent

class LiveEventDemoReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_LIVE_EVENT_DEMO_BROADCAST) {
            return
        }
        GlobalLiveEvent.sendMessage(CODE_RECEIVER_RESPONSE, "BroadcastReceiver收到广播并发送LiveEvent消息")
    }
}
