package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockTrackingClient
import com.kickstarter.libs.TrackingClientType
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.User
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import rx.observers.TestSubscriber

@HiltAndroidTest
class SettingsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: SettingsViewModel.ViewModel
    private val currentUserTest = TestSubscriber<User>()
    private val logout = TestSubscriber<Void>()
    private val showConfirmLogoutPrompt = TestSubscriber<Boolean>()
    private val currentUser = TestSubscriber<User?>()

    private fun setUpEnvironment(user: User) {
        val currentUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
            .currentUser(currentUser)
            .analytics(AnalyticEvents(listOf(getMockClientWithUser(user))))
            .build()

        setUpEnvironment(environment)
        currentUser.observable().subscribe(this.currentUserTest)
    }

    private fun setUpEnvironment(environment: Environment) {

        this.vm = SettingsViewModel.ViewModel(environment)
        this.vm.outputs.logout().subscribe(this.logout)
        this.vm.outputs.showConfirmLogoutPrompt().subscribe(this.showConfirmLogoutPrompt)
    }

    @Test
    fun testConfirmLogoutClicked() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.confirmLogoutClicked()
        this.logout.assertValueCount(1)
    }

    @Test
    fun testUserEmits() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)
        this.vm.outputs.showConfirmLogoutPrompt().subscribe(showConfirmLogoutPrompt)
    }

    @Test
    fun testShowConfirmLogoutPrompt() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.currentUserTest.assertValues(user)

        this.vm.inputs.logoutClicked()
        this.showConfirmLogoutPrompt.assertValue(true)
    }

    @Test
    fun user_whenPressLogout_userReset() {
        val user = UserFactory.user()

        setUpEnvironment(user)

        this.vm.inputs.confirmLogoutClicked()
        this.logout.assertValue(null)

        this.currentUser.assertValues(user, null)
    }

    private fun getMockClientWithUser(user: User) = MockTrackingClient(
        MockCurrentUser(user),
        MockCurrentConfig(),
        TrackingClientType.Type.SEGMENT,
        MockExperimentsClientType()
    ).apply {
        this.identifiedUser.subscribe(currentUser)
    }
}
