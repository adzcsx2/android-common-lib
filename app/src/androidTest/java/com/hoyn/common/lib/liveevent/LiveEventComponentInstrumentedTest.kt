package com.hoyn.common.lib.liveevent

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hoyn.common.base.event.GlobalLiveEvent
import com.hoyn.common.core.Message
import com.hoyn.common.lib.ui.liveevent.ACTION_LIVE_EVENT_DEMO_BROADCAST
import com.hoyn.common.lib.ui.liveevent.CODE_RECEIVER_RESPONSE
import com.hoyn.common.lib.ui.liveevent.CODE_SERVICE_REQUEST
import com.hoyn.common.lib.ui.liveevent.CODE_SERVICE_RESPONSE
import com.hoyn.common.lib.ui.liveevent.LiveEventDemoReceiver
import com.hoyn.common.lib.ui.liveevent.LiveEventDemoService
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class LiveEventComponentInstrumentedTest {

    @Test
    fun serviceReceivesAndRespondsThroughGlobalLiveEvent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val startupLatch = CountDownLatch(1)
        val responseLatch = CountDownLatch(1)

        val observer = GlobalLiveEvent.subscribeMessage { message ->
            when {
                message.code == CODE_SERVICE_RESPONSE && message.msg.contains("已启动") -> startupLatch.countDown()
                message.code == CODE_SERVICE_RESPONSE && message.msg.contains("Service收到LiveEvent消息") -> responseLatch.countDown()
            }
        }

        try {
            context.startService(Intent(context, LiveEventDemoService::class.java))
            assertTrue(startupLatch.await(5, TimeUnit.SECONDS))

            GlobalLiveEvent.sendMessage(CODE_SERVICE_REQUEST, "instrumentation ping")
            assertTrue(responseLatch.await(5, TimeUnit.SECONDS))
        } finally {
            context.stopService(Intent(context, LiveEventDemoService::class.java))
            GlobalLiveEvent.removeMessageObserver(observer)
        }
    }

    @Test
    fun receiverForwardsBroadcastIntoGlobalLiveEvent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val latch = CountDownLatch(1)

        val observer = GlobalLiveEvent.subscribeMessage { message ->
            if (message.code == CODE_RECEIVER_RESPONSE && message.msg.contains("BroadcastReceiver收到广播")) {
                latch.countDown()
            }
        }

        try {
            val intent = Intent(context, LiveEventDemoReceiver::class.java).apply {
                action = ACTION_LIVE_EVENT_DEMO_BROADCAST
            }
            context.sendBroadcast(intent)

            assertTrue(latch.await(5, TimeUnit.SECONDS))
        } finally {
            GlobalLiveEvent.removeMessageObserver(observer)
        }
    }
}