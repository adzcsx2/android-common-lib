package com.hoyn.common.base.event

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

class BaseLiveEventTest {

    @Test
    fun testBaseLiveEventClassName() {
        assertTrue(BaseLiveEvent::class.java.simpleName == "BaseLiveEvent")
    }

    @Test
    fun testBaseLiveEventStructure() {
        val modifiers = BaseLiveEvent::class.java.modifiers
        assertTrue("BaseLiveEvent should be abstract", Modifier.isAbstract(modifiers))
    }

    @Test
    fun testSingleLiveEventUsesBaseLiveEvent() {
        assertTrue(SingleLiveEvent::class.java.superclass == BaseLiveEvent::class.java)
    }
}
