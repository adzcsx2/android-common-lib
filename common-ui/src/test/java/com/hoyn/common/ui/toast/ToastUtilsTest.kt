package com.hoyn.common.ui.toast

import com.hjq.toast.ToastParams
import com.hjq.toast.config.IToastInterceptor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToastUtilsTest {

    @Before
    fun setUp() {
        ToastConfig.yOffsetPxComputer = { 192 }
    }

    @After
    fun tearDown() {
        ToastConfig.yOffsetPxComputer = {
            android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP,
                ToastConfig.DEFAULT_BOTTOM_Y_OFFSET_DP,
                android.content.res.Resources.getSystem().displayMetrics
            ).toInt()
        }
    }

    @Test
    fun `findCallerStackTrace returns first non filtered frame when stackSkips is zero`() {
        val inlineFrame = StackTraceElement(
            "com.hoyn.common.lib.ui.toast_demo.ToastDemoActivity\$setupViews\$\$inlined\$click\$default\$2",
            "onClick",
            "ViewExtensions.kt",
            577
        )
        val activityFrame = StackTraceElement(
            "com.hoyn.common.lib.ui.toast_demo.ToastDemoActivity",
            "setupViews",
            "ToastDemoActivity.kt",
            64
        )
        val stack = arrayOf(
            StackTraceElement(
                "com.hoyn.common.ui.toast.ToastUtils\$CallSiteToastInterceptor",
                "intercept",
                "ToastUtils.kt",
                530
            ),
            StackTraceElement(
                "com.hoyn.common.ui.toast.ToastUtils",
                "show",
                "ToastUtils.kt",
                140
            ),
            StackTraceElement(
                "com.hjq.toast.Toaster",
                "show",
                "Toaster.java",
                180
            ),
            inlineFrame,
            activityFrame
        )

        val caller = ToastCallerLocator.findCallerStackTrace(stack, stackSkips = 0) { className ->
            when (className) {
                "com.hoyn.common.ui.toast.ToastUtils\$CallSiteToastInterceptor" -> FakeInterceptor::class.java
                inlineFrame.className -> FakeInlineCaller::class.java
                activityFrame.className -> FakeCaller::class.java
                else -> null
            }
        }

        assertEquals(inlineFrame, caller)
    }

    @Test
    fun `findCallerStackTrace skips wrapper frame when stackSkips is one`() {
        val inlineFrame = StackTraceElement(
            "com.hoyn.common.lib.ui.toast_demo.ToastDemoActivity\$setupViews\$\$inlined\$click\$default\$2",
            "onClick",
            "ViewExtensions.kt",
            577
        )
        val activityFrame = StackTraceElement(
            "com.hoyn.common.lib.ui.toast_demo.ToastDemoActivity",
            "setupViews",
            "ToastDemoActivity.kt",
            64
        )
        val stack = arrayOf(
            StackTraceElement(
                "com.hoyn.common.ui.toast.ToastUtils\$CallSiteToastInterceptor",
                "intercept",
                "ToastUtils.kt",
                530
            ),
            StackTraceElement(
                "com.hoyn.common.ui.toast.ToastUtils",
                "show",
                "ToastUtils.kt",
                140
            ),
            StackTraceElement(
                "com.hjq.toast.Toaster",
                "show",
                "Toaster.java",
                180
            ),
            inlineFrame,
            activityFrame
        )

        val caller = ToastCallerLocator.findCallerStackTrace(
            stack,
            stackSkips = DEFAULT_TOAST_STACK_SKIPS
        ) { className ->
            when (className) {
                "com.hoyn.common.ui.toast.ToastUtils\$CallSiteToastInterceptor" -> FakeInterceptor::class.java
                inlineFrame.className -> FakeInlineCaller::class.java
                activityFrame.className -> FakeCaller::class.java
                else -> null
            }
        }

        assertEquals(activityFrame, caller)
    }

    @Test
    fun `showCenter config lambda overrides preset defaults`() {
        val resolved = ToastConfig.defaults().apply {
            gravity = android.view.Gravity.CENTER
            yOffset = 0
            // user overrides should win
            yOffset = 50
            xOffset = 10
        }

        assertEquals(android.view.Gravity.CENTER, resolved.gravity)
        assertEquals(50, resolved.yOffset)
        assertEquals(10, resolved.xOffset)
    }

    @Test
    fun `findCallerStackTrace keeps direct call frame with zero skips`() {
        val directFrame = StackTraceElement(
            "com.hoyn.common.lib.ui.compose.ComposeDemoViewModel",
            "showToast",
            "ComposeDemoViewModel.kt",
            91
        )
        val parentFrame = StackTraceElement(
            "com.hoyn.common.lib.ui.compose.ComposeDemoScreenKt",
            "onClick",
            "ComposeDemoScreen.kt",
            45
        )
        val stack = arrayOf(
            StackTraceElement(
                "com.hoyn.common.ui.toast.ToastUtils\$CallSiteToastInterceptor",
                "intercept",
                "ToastUtils.kt",
                530
            ),
            StackTraceElement(
                "com.hoyn.common.ui.toast.ToastUtils",
                "show",
                "ToastUtils.kt",
                140
            ),
            StackTraceElement(
                "com.hjq.toast.Toaster",
                "show",
                "Toaster.java",
                180
            ),
            directFrame,
            parentFrame
        )

        val caller = ToastCallerLocator.findCallerStackTrace(
            stack,
            stackSkips = 0
        ) { className ->
            when (className) {
                "com.hoyn.common.ui.toast.ToastUtils\$CallSiteToastInterceptor" -> FakeInterceptor::class.java
                directFrame.className -> FakeCaller::class.java
                parentFrame.className -> FakeParentCaller::class.java
                else -> null
            }
        }

        assertEquals(directFrame, caller)
    }

    @Test
    fun `resolveDisplayLocation keeps normal frame unchanged`() {
        val frame = StackTraceElement(
            "com.hoyn.common.lib.ui.toast_demo.ToastDemoActivity",
            "setupViews",
            "ToastDemoActivity.kt",
            64
        )

        val location = ToastCallerLocator.resolveDisplayLocation(frame)

        assertEquals("ToastDemoActivity.kt", location.fileName)
        assertEquals(64, location.lineNumber)
        assertEquals("(ToastDemoActivity.kt:64)", location.asLogPrefix())
    }

    @Test
    fun `resolveDisplayLocation keeps inline wrapper frame unchanged`() {
        val inlineFrame = StackTraceElement(
            "com.hoyn.common.lib.ui.toast_demo.ToastDemoActivity\$setupViews\$\$inlined\$click\$default\$2",
            "onClick",
            "ViewExtensions.kt",
            577
        )

        val location = ToastCallerLocator.resolveDisplayLocation(inlineFrame)

        assertEquals("ViewExtensions.kt", location.fileName)
        assertEquals(577, location.lineNumber)
        assertEquals("(ViewExtensions.kt:577)", location.asLogPrefix())
    }

    @Test
    fun `findCallerStackTrace returns null when all frames are filtered`() {
        val stack = arrayOf(
            StackTraceElement(
                "com.hoyn.common.ui.toast.ToastUtils\$CallSiteToastInterceptor",
                "intercept",
                "ToastUtils.kt",
                530
            ),
            StackTraceElement(
                "com.hoyn.common.ui.toast.ToastUtils",
                "show",
                "ToastUtils.kt",
                140
            ),
            StackTraceElement(
                "com.hjq.toast.Toaster",
                "show",
                "Toaster.java",
                180
            )
        )

        val caller = ToastCallerLocator.findCallerStackTrace(stack) { className ->
            when (className) {
                "com.hoyn.common.ui.toast.ToastUtils\$CallSiteToastInterceptor" -> FakeInterceptor::class.java
                else -> null
            }
        }

        assertNull(caller)
    }

    @Test
    fun `compose keeps global interceptor active`() {
        val events = mutableListOf<String>()
        val callSiteInterceptor = IToastInterceptor {
            events += "call-site"
            false
        }
        val globalInterceptor = IToastInterceptor {
            events += "global"
            false
        }

        val interceptor = ToastInterceptorComposer.compose(callSiteInterceptor, globalInterceptor)

        val intercepted = interceptor.intercept(ToastParams().apply { text = "hello" })

        assertFalse(intercepted)
        assertEquals(listOf("call-site", "global"), events)
    }

    @Test
    fun `compose short circuits when call site interceptor intercepts`() {
        val events = mutableListOf<String>()
        val callSiteInterceptor = IToastInterceptor {
            events += "call-site"
            true
        }
        val globalInterceptor = IToastInterceptor {
            events += "global"
            false
        }

        val interceptor = ToastInterceptorComposer.compose(callSiteInterceptor, globalInterceptor)

        val intercepted = interceptor.intercept(ToastParams().apply { text = "hello" })

        assertTrue(intercepted)
        assertEquals(listOf("call-site"), events)
    }

    @Test
    fun `copyOf keeps original params unchanged`() {
        val originalInterceptor = IToastInterceptor { false }
        val replacementInterceptor = IToastInterceptor { false }
        val source = ToastParams().apply {
            text = "source"
            duration = 1
            delayMillis = 2L
            priorityType = ToastParams.PRIORITY_TYPE_GLOBAL
            interceptor = originalInterceptor
        }

        val copied = ToastParamsMapper.copyOf(source, "target", replacementInterceptor)

        assertEquals("source", source.text)
        assertEquals(originalInterceptor, source.interceptor)
        assertEquals("target", copied.text)
        assertEquals(replacementInterceptor, copied.interceptor)
        assertEquals(source.duration, copied.duration)
        assertEquals(source.delayMillis, copied.delayMillis)
        assertEquals(source.priorityType, copied.priorityType)
    }

    @Test
    fun `stackSkips store returns zero after take`() {
        val params = ToastParams().apply { text = "hello" }

        ToastStackSkipsStore.bind(params, 1)

        assertEquals(1, ToastStackSkipsStore.take(params))
        assertEquals(0, ToastStackSkipsStore.take(params))
    }

    @Test
    fun `stackSkips store keeps custom skip value`() {
        val params = ToastParams().apply { text = "hello" }

        ToastStackSkipsStore.bind(params, 2)

        assertEquals(2, ToastStackSkipsStore.take(params))
    }

    @Test
    fun `ToastConfig defaults have expected values`() {
        val config = ToastConfig.defaults()

        assertEquals(android.view.Gravity.BOTTOM, config.gravity)
        assertEquals(0, config.xOffset)
        assertEquals(DEFAULT_TOAST_STACK_SKIPS, config.stackSkips)
        assertTrue(config.yOffset > 0)
    }

    @Test
    fun `ToastConfig copy produces independent instance`() {
        val original = ToastConfig.defaults()
        val copy = original.copy()

        copy.gravity = android.view.Gravity.CENTER
        copy.xOffset = 100
        copy.stackSkips = 5

        assertEquals(android.view.Gravity.BOTTOM, original.gravity)
        assertEquals(0, original.xOffset)
        assertEquals(DEFAULT_TOAST_STACK_SKIPS, original.stackSkips)
    }

    @Test
    fun `ToastConfig DSL modifies properties`() {
        val config = ToastConfig.defaults()
        config.apply {
            gravity = android.view.Gravity.CENTER
            xOffset = 10
            yOffset = 20
            stackSkips = 3
        }

        assertEquals(android.view.Gravity.CENTER, config.gravity)
        assertEquals(10, config.xOffset)
        assertEquals(20, config.yOffset)
        assertEquals(3, config.stackSkips)
    }

    private class FakeCaller
    private class FakeInlineCaller
    private class FakeParentCaller

    private class FakeInterceptor : IToastInterceptor {
        override fun intercept(params: ToastParams): Boolean = false
    }
}
