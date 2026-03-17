package com.hoyn.common.base

import androidx.lifecycle.ViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewModelClassResolverTest {

    @Test
    fun resolvesActivityViewModelFromDirectSubclass() {
        val resolved = ViewModelClassResolver.resolve<ActivityViewModel>(
            TestActivityScreen(),
            TestActivityBase::class.java
        )

        assertEquals(ActivityViewModel::class.java, resolved)
    }

    @Test
    fun resolvesFragmentViewModelThroughIntermediateClass() {
        val resolved = ViewModelClassResolver.resolve<FragmentViewModel>(
            TestFragmentScreen(),
            TestFragmentBase::class.java
        )

        assertEquals(FragmentViewModel::class.java, resolved)
    }

    @Test(expected = IllegalStateException::class)
    fun throwsWhenBaseClassIsNotInHierarchy() {
        ViewModelClassResolver.resolve<FragmentViewModel>(
            TestFragmentScreen(),
            UnrelatedBase::class.java
        )
    }

    private abstract class TestActivityBase<VB, VM : ViewModel>

    private class TestActivityScreen : TestActivityBase<String, ActivityViewModel>()

    private abstract class TestFragmentBase<VB, VM : ViewModel>

    private abstract class TestFragmentIntermediate<VB, VM : ViewModel> : TestFragmentBase<VB, VM>()

    private class TestFragmentScreen : TestFragmentIntermediate<String, FragmentViewModel>()

    private abstract class UnrelatedBase<VB, VM : ViewModel>

    private class ActivityViewModel : ViewModel()

    private class FragmentViewModel : ViewModel()
}