package com.hoyn.common.lib.liveevent

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hoyn.common.base.event.GlobalLiveEvent
import com.hoyn.common.core.Message
import com.hoyn.common.lib.ui.liveevent.LiveEventDemoActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

@RunWith(AndroidJUnit4::class)
class GlobalLiveEventInstrumentedTest {

    @Test
    fun normalObserverDoesNotReceivePreviousMessage() {
        val received = CopyOnWriteArrayList<Message>()

        ActivityScenario.launch(LiveEventDemoActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                GlobalLiveEvent.sendMessage(9001, "before normal observe")
                GlobalLiveEvent.observeMessage(activity) { message ->
                    if (message.code == 9001) {
                        received += message
                    }
                }
            }
        }

        assertTrue(received.isEmpty())
    }

    @Test
    fun stickyObserverReceivesPreviousMessage() {
        val latch = CountDownLatch(1)
        val received = CopyOnWriteArrayList<Message>()

        ActivityScenario.launch(LiveEventDemoActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                GlobalLiveEvent.sendMessage(9002, "before sticky observe")
                GlobalLiveEvent.observeStickyMessage(activity) { message ->
                    if (message.code == 9002) {
                        received += message
                        latch.countDown()
                    }
                }
            }
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertEquals(1, received.size)
    }

    @Test
    fun lifecycleObserverStopsAfterDestroy() {
        val deliveries = AtomicInteger(0)

        val scenario = ActivityScenario.launch(LiveEventDemoActivity::class.java)
        scenario.onActivity { activity ->
            GlobalLiveEvent.observeMessage(activity) { message ->
                if (message.code == 9003) {
                    deliveries.incrementAndGet()
                }
            }
        }
        scenario.close()

        GlobalLiveEvent.sendMessage(9003, "after destroy")

        assertEquals(0, deliveries.get())
    }

    @Test
    fun manualObserverStopsAfterRemove() {
        val deliveries = AtomicInteger(0)
        val observer = GlobalLiveEvent.subscribeMessage { message ->
            if (message.code == 9004) {
                deliveries.incrementAndGet()
            }
        }

        GlobalLiveEvent.removeMessageObserver(observer)
        GlobalLiveEvent.sendMessage(9004, "after remove")

        assertEquals(0, deliveries.get())
    }

    @Test
    fun multithreadedSendDeliversAllMessages() {
        val total = 12
        val receivedCodes = Collections.synchronizedSet(mutableSetOf<Int>())
        val latch = CountDownLatch(total)
        val observer = GlobalLiveEvent.subscribeMessage { message ->
            if (message.code in 9100 until 9100 + total) {
                if (receivedCodes.add(message.code)) {
                    latch.countDown()
                }
            }
        }

        try {
            val workers = (0 until total).map { offset ->
                thread(start = true) {
                    GlobalLiveEvent.sendMessage(9100 + offset, "thread-$offset")
                }
            }
            workers.forEach { it.join() }

            assertTrue(latch.await(5, TimeUnit.SECONDS))
            assertEquals(total, receivedCodes.size)
        } finally {
            GlobalLiveEvent.removeMessageObserver(observer)
        }
    }
}
