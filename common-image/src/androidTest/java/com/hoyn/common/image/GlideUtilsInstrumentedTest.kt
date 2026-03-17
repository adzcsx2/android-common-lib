package com.hoyn.common.image

import android.content.Context
import android.os.SystemClock
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hoyn.common.utils.PxUtils.dp
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class GlideUtilsInstrumentedTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val appContext: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun loadImage_withResource_setsDrawable() {
        val imageView = ImageView(appContext)

        instrumentation.runOnMainSync {
            imageView.loadImage(android.R.drawable.ic_menu_gallery)
        }

        assertTrue(waitForDrawable(imageView))
    }

    @Test
    fun loadCircleImage_withResource_setsDrawable() {
        val imageView = ImageView(appContext)

        instrumentation.runOnMainSync {
            imageView.loadCircleImage(android.R.drawable.ic_menu_camera)
        }

        assertTrue(waitForDrawable(imageView))
    }

    @Test
    fun loadRoundedImage_withResource_setsDrawable() {
        val imageView = ImageView(appContext)

        instrumentation.runOnMainSync {
            imageView.loadRoundedImage(
                resourceId = android.R.drawable.ic_menu_report_image,
                radiusPx = 10.dp
            )
        }

        assertTrue(waitForDrawable(imageView))
    }

    @Test
    fun clear_and_cache_apis_doNotThrow() {
        val imageView = ImageView(appContext)

        instrumentation.runOnMainSync {
            imageView.loadImage(android.R.drawable.ic_menu_gallery)
            GlideUtils.clear(imageView)
            GlideUtils.clearMemory(appContext)
        }

        GlideUtils.clearDiskCache(appContext)
    }

    private fun waitForDrawable(
        imageView: ImageView,
        timeoutMs: Long = 3_000L
    ): Boolean {
        val startTime = SystemClock.elapsedRealtime()
        while (SystemClock.elapsedRealtime() - startTime < timeoutMs) {
            val hasDrawable = AtomicBoolean(false)
            instrumentation.runOnMainSync {
                hasDrawable.set(imageView.drawable != null)
            }
            if (hasDrawable.get()) {
                return true
            }
            SystemClock.sleep(50)
        }
        return false
    }
}