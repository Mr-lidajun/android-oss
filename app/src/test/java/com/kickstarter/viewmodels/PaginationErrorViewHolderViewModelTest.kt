package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import rx.observers.TestSubscriber

@HiltAndroidTest
class PaginationErrorViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: PaginationErrorViewHolderViewModel.ViewModel

    private val isErrorCellVisible = TestSubscriber<Boolean>()

    private fun setupEnvironment() {
        this.vm = PaginationErrorViewHolderViewModel.ViewModel(environment())
        this.vm.outputs.isErrorPaginationVisible().subscribe(isErrorCellVisible)
    }

    @Test
    fun isErrorCellUIVisible() {
        setupEnvironment()

        this.vm.inputs.configureWith(true)
        this.isErrorCellVisible.assertValue(true)
    }

    @Test
    fun isErrorCellUINotVisible() {
        setupEnvironment()

        this.vm.inputs.configureWith(false)
        this.isErrorCellVisible.assertValue(false)
    }
}
