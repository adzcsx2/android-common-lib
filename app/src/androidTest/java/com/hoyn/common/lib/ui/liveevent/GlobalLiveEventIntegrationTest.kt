package com.hoyn.common.lib.ui.liveevent

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hoyn.common.base.event.GlobalLiveEvent
import com.hoyn.common.base.event.Message
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class GlobalLiveEventIntegrationTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val trackedObservers = mutableListOf<Observer<Message>>()

    @After
    fun tearDown() {
        trackedObservers.forEach(GlobalLiveEvent::removeMessageObserver)
        trackedObservers.clear()
        context.stopService(Intent(context, LiveEventDemoService::class.java))
    }

    @Test
    fun lifecycleObserverStopsReceivingAfterDestroy() {
        val owner = TestLifecycleOwner()
        owner.moveTo(Lifecycle.State.CREATED)

        val receivedCount = AtomicInteger(0)
        GlobalLiveEvent.observeMessage(owner) { message ->
            if (message.code == 8101) {
                receivedCount.incrementAndGet()
            }
        }

        GlobalLiveEvent.sendMessage(8101, "first")
        owner.moveTo(Lifecycle.State.DESTROYED)
        GlobalLiveEvent.sendMessage(8101, "second")

        assertEquals(1, receivedCount.get())
    }

    @Test
    fun multithreadedPostsDeliverAllMessages() {
        val total = 24
        val latch = CountDownLatch(total)
        val receivedCodes = Collections.synchronizedSet(mutableSetOf<Int>())
        val observer = GlobalLiveEvent.subscribeMessage { message ->
            if (message.code in 9000 until 9000 + total) {
                receivedCodes.add(message.code)
                latch.countDown()
            }
        }
        trackedObservers.add(observer)

        val executor = Executors.newFixedThreadPool(6)
        repeat(total) { index ->
            executor.execute {
                GlobalLiveEvent.sendMessage(9000 + index, "msg-$index")
            }
        }
        executor.shutdown()

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(total, receivedCodes.size)
    }

    @Test
    fun serviceReceivesAndRespondsToLiveEvent() {
        val latch = CountDownLatch(2)
        val observer = GlobalLiveEvent.subscribeMessage { message ->
            if (message.code == CODE_SERVICE_RESPONSE) {
                latch.countDown()
            }
        }
        trackedObservers.add(observer)

        context.startService(Intent(context, LiveEventDemoService::class.java))
        GlobalLiveEvent.sendMessage(CODE_SERVICE_REQUEST, "service-test")

        assertTrue(latch.await(5, TimeUnit.SECONDS))
    }

    @Test
    fun broadcastReceiverForwardsToGlobalLiveEvent() {
        val latch = CountDownLatch(1)
        val observer = GlobalLiveEvent.subscribeMessage { message ->
            if (message.code == CODE_RECEIVER_RESPONSE) {
                latch.countDown()
            }
        }
        trackedObservers.add(observer)

        context.sendBroadcast(Intent(context, LiveEventDemoReceiver::class.java).apply {
            action = ACTION_LIVE_EVENT_DEMO_BROADCAST
        })

        assertTrue(latch.await(5, TimeUnit.SECONDS))
    }

    private class TestLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle
            get() = registry

        fun moveTo(state: Lifecycle.State) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                registry.currentState = state
            }
        }
    }
}
