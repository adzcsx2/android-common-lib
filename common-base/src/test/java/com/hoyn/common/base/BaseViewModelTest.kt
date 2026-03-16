package com.hoyn.common.base

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    @get
    @get
    val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var application: Application
    private lateinit var viewModel: TestableViewModel

    @Before
    fun setup() {
        application = Application()
        viewModel = TestableViewModel(application, testDispatcher)
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `initial toastEvent should be empty`() {
        assertEquals(null, viewModel.defUI.toastEvent.value)
    }

    @Test
    fun `initial showDialog should be empty`() {
        assertEquals(null, viewModel.defUI.showDialog.value)
    }

    @Test
    fun `initial dismissDialog should be empty`() {
        assertEquals(null, viewModel.defUI.dismissDialog.value)
    }

    @Test
    fun `initial errorEvent should be empty`() {
        assertEquals(null, viewModel.defUI.errorEvent.value)
    }

    @Test
    fun `initial msgEvent should be empty`() {
        assertEquals(null, viewModel.defUI.msgEvent.value)
    }

    @Test
    fun `showToast should update toastEvent`() {
        viewModel.showToast("Test Toast")

        assertEquals("Test Toast", viewModel.defUI.toastEvent.value)
    }

    @Test
    fun `showDialog should update showDialog`() {
        viewModel.showDialog("Test Dialog")

        assertEquals("Test Dialog", viewModel.defUI.showDialog.value)
    }

    @Test
    fun `showDialog with null should update showDialog with null`() {
        viewModel.showDialog(null)

        assertNull(viewModel.defUI.showDialog.value)
    }

    @Test
    fun `dismissDialog should update dismissDialog`() {
        viewModel.dismissDialog()

        assertNotNull(viewModel.defUI.dismissDialog.value)
    }

    @Test
    fun `sendError should update errorEvent with ThrowableBean`() {
        val error = ThrowableBean(404, "Not Found")
        viewModel.sendError(error)

        assertEquals(error, viewModel.defUI.errorEvent.value)
    }

    @Test
    fun `sendError with exception should create ThrowableBean`() {
        val exception = RuntimeException("Test error")
        viewModel.sendError(exception)

        val errorEvent = viewModel.defUI.errorEvent.value
        assertNotNull(errorEvent)
        assertEquals(-1, errorEvent.code)
        assertEquals("Test error", errorEvent.errMsg)
    }

    @Test
    fun `sendMessage should update msgEvent`() {
        val message = com.hoyn.common.core.Message(code = 200, msg = "Test Message")
        viewModel.sendMessage(message)

        assertEquals(message, viewModel.defUI.msgEvent.value)
    }

    @Test
    fun `launchUI should execute on Main dispatcher`() = runTest {
        var executed = false
        val testViewModel = object : TestableViewModel(application, testDispatcher) {
            override fun onLaunchUI() {
                executed = true
            }
        }

        testViewModel.testLaunchUI()

        assertTrue(executed)
    }

    @Test
    fun `launchIO should execute on IO dispatcher`() = runTest {
        var executed = false
        val testViewModel = object : TestableViewModel(application, testDispatcher) {
            override fun onLaunchIO() {
                executed = true
            }
        }

        testViewModel.testLaunchIO()

        assertTrue(executed)
    }

    @Test
    fun `launchFlow should execute flow`() = runTest {
        var executed = false
        val testViewModel = object : TestableViewModel(application, testDispatcher) {
            override fun onLaunchFlow() {
                executed = true
            }
        }

        testViewModel.testLaunchFlow()

        assertTrue(executed)
    }

    @Test
    fun `handleException should convert exception to ThrowableBean`() {
        val exception = RuntimeException("Test exception")
        val result = viewModel.testHandleException(exception)

        assertNotNull(result)
        assertEquals(-1, result.code)
        assertEquals("Test exception", result.errMsg)
    }

    /**
     * Test implementation of BaseViewModel for testing
     */
    private class TestableViewModel(application: Application, testDispatcher: Dispatchers) :
        BaseViewModel<Any>(application) {

        fun testLaunchUI() {
            launchUI { /* Test UI launch */ }
        }

        fun testLaunchIO() {
            launchIO { /* Test IO launch */ }
        }

        fun testLaunchFlow() {
            launchFlow { /* Test flow launch */ }
        }

        fun testHandleException(exception: Exception): ThrowableBean {
            return super.handleException(exception)
        }
    }
}
