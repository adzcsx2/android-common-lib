package com.hoyn.common.base

import android.app.Activity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.kotlin.mock

class ActivityStackManagerTest {

    @After
    fun tearDown() {
        clearStack()
    }

    @Test
    fun registerActivityDeduplicatesAndKeepsInsertionOrder() {
        val firstActivity = mock<Activity>()
        val secondActivity = mock<Activity>()

        ActivityStackManager.registerActivity(firstActivity)
        ActivityStackManager.registerActivity(firstActivity)
        ActivityStackManager.registerActivity(secondActivity)

        val snapshot = ActivityStackManager.getActivityStackSnapshot()

        assertEquals(2, snapshot.size)
        assertSame(firstActivity, snapshot[0])
        assertSame(secondActivity, snapshot[1])
        assertSame(secondActivity, ActivityStackManager.current())
    }

    @Test
    fun unregisterActivityRemovesItFromSnapshotAndCurrent() {
        val firstActivity = mock<Activity>()
        val secondActivity = mock<Activity>()

        ActivityStackManager.registerActivity(firstActivity)
        ActivityStackManager.registerActivity(secondActivity)
        ActivityStackManager.unregisterActivity(secondActivity)

        val snapshot = ActivityStackManager.getActivityStackSnapshot()

        assertEquals(1, snapshot.size)
        assertSame(firstActivity, snapshot.single())
        assertSame(firstActivity, ActivityStackManager.current())
    }

    private fun clearStack() {
        ActivityStackManager.getActivityStackSnapshot().forEach(ActivityStackManager::unregisterActivity)
    }
}