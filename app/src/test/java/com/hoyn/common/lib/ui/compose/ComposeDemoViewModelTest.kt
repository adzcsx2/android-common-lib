package com.hoyn.common.lib.ui.compose

import android.app.Application
import android.content.Context
import com.hoyn.common.core.UIState
import com.hoyn.common.lib.data.model.Post
import com.hoyn.common.lib.data.repository.PostLoadResult
import com.hoyn.common.lib.data.repository.PostRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeDemoViewModelTest {

    private lateinit var mockApplication: Application
    private lateinit var mockRepository: PostRepository
    private lateinit var viewModel: ComposeDemoViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val samplePosts = listOf(
        Post(userId = 1, id = 1, title = "Compose Post 1", body = "Body 1"),
        Post(userId = 2, id = 2, title = "Compose Post 2", body = "Body 2")
    )

    @Before
    fun setup() {
        mockApplication = mock()
        mockRepository = mock()
        whenever(mockApplication.applicationContext).thenReturn(mock<Context>())
        viewModel = ComposeDemoViewModel(mockApplication, mockRepository, testDispatcher)
    }

}
