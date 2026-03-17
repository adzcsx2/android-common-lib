package com.hoyn.common.lib.ui.fragment_demo

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hoyn.common.lib.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentDemoActivityTest {

    @Test
    fun fragmentViewModelRetainsStateAfterActivityRecreation() {
        ActivityScenario.launch(FragmentDemoActivity::class.java).use { scenario ->
            lateinit var initialState: FragmentDemoUiState

            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                    as FragmentDemoFragment
                fragment.incrementForTest(3)
            }

            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                    as FragmentDemoFragment
                initialState = fragment.viewModelStateForTest()
                assertEquals(3, initialState.count)
            }

            scenario.recreate()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                    as FragmentDemoFragment
                val firstRecreatedState = fragment.viewModelStateForTest()

                assertEquals(initialState.sessionId, firstRecreatedState.sessionId)
                assertEquals(initialState.count, firstRecreatedState.count)
                assertEquals(3, firstRecreatedState.count)
            }

            scenario.recreate()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                    as FragmentDemoFragment
                val secondRecreatedState = fragment.viewModelStateForTest()

                assertEquals(initialState.sessionId, secondRecreatedState.sessionId)
                assertEquals(initialState.count, secondRecreatedState.count)
                assertEquals(3, secondRecreatedState.count)
                assertNotNull(fragment.view)
            }
        }
    }

    @Test
    fun fragmentViewModelConsumesSavedStateDefaultArgs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val initialSessionId = "restored-session"
        val initialCount = 7

        ActivityScenario.launch<FragmentDemoActivity>(
            FragmentDemoActivity.createIntent(
                context = context,
                initialSessionId = initialSessionId,
                initialCount = initialCount
            )
        ).use { scenario ->
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                    as FragmentDemoFragment
                val state = fragment.viewModelStateForTest()

                assertEquals(initialSessionId, state.sessionId)
                assertEquals(initialCount, state.count)
            }
        }
    }
}