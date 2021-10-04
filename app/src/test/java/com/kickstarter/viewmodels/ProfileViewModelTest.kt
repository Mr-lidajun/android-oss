package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.DiscoverEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ProfileViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProfileViewModel.ViewModel
    private val avatarImageViewUrl = TestSubscriber<String>()
    private val backedCountTextViewHidden = TestSubscriber<Boolean>()
    private val backedCountTextViewText = TestSubscriber<String>()
    private val backedTextViewHidden = TestSubscriber<Boolean>()
    private val createdCountTextViewHidden = TestSubscriber<Boolean>()
    private val createdCountTextViewText = TestSubscriber<String>()
    private val createdTextViewHidden = TestSubscriber<Boolean>()
    private val dividerViewHidden = TestSubscriber<Boolean>()
    private val projectList = TestSubscriber<List<Project>>()
    private val resumeDiscoveryActivity = TestSubscriber<Void>()
    private val startMessageThreadsActivity = TestSubscriber<Void>()
    private val startProjectActivity = TestSubscriber<Pair<Project, Boolean>>()
    private val userNameTextViewText = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProfileViewModel.ViewModel(environment)

        this.vm.outputs.avatarImageViewUrl().subscribe(this.avatarImageViewUrl)
        this.vm.outputs.backedCountTextViewHidden().subscribe(this.backedCountTextViewHidden)
        this.vm.outputs.backedCountTextViewText().subscribe(this.backedCountTextViewText)
        this.vm.outputs.backedTextViewHidden().subscribe(this.backedTextViewHidden)
        this.vm.outputs.createdCountTextViewHidden().subscribe(this.createdCountTextViewHidden)
        this.vm.outputs.createdCountTextViewText().subscribe(this.createdCountTextViewText)
        this.vm.outputs.createdTextViewHidden().subscribe(this.createdTextViewHidden)
        this.vm.outputs.dividerViewHidden().subscribe(this.dividerViewHidden)
        this.vm.outputs.projectList().subscribe(this.projectList)
        this.vm.outputs.resumeDiscoveryActivity().subscribe(this.resumeDiscoveryActivity)
        this.vm.outputs.startMessageThreadsActivity().subscribe(this.startMessageThreadsActivity)
        this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity)
        this.vm.outputs.userNameTextViewText().subscribe(this.userNameTextViewText)
    }

    @Test
    fun testProfileViewModel_EmitsBackedAndCreatedProjectsData() {
        val user = UserFactory.user().toBuilder()
            .backedProjectsCount(15)
            .createdProjectsCount(2)
            .build()

        val apiClient = object : MockApiClient() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        // Backed text views are displayed.
        this.backedCountTextViewHidden.assertValues(false)
        this.backedCountTextViewText.assertValues(NumberUtils.format(user.backedProjectsCount()!!))
        this.backedTextViewHidden.assertValues(false)

        // Created text views are displayed.
        this.createdCountTextViewHidden.assertValues(false)
        this.createdCountTextViewText.assertValues(NumberUtils.format(user.createdProjectsCount()!!))
        this.createdTextViewHidden.assertValues(false)

        // Divider view is displayed.
        this.dividerViewHidden.assertValues(false)
    }

    @Test
    fun testProfileViewModel_EmitsBackedProjectsData() {
        val user = UserFactory.user().toBuilder()
            .backedProjectsCount(5)
            .createdProjectsCount(0)
            .build()

        val apiClient = object : MockApiClient() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        // Backed text views are displayed.
        this.backedCountTextViewHidden.assertValues(false)
        this.backedCountTextViewText.assertValues(NumberUtils.format(user.backedProjectsCount()!!))
        this.backedTextViewHidden.assertValues(false)

        // Created text views are hidden.
        this.createdCountTextViewHidden.assertValues(true)
        this.createdCountTextViewText.assertNoValues()
        this.createdTextViewHidden.assertValues(true)

        // Divider view is hidden.
        this.dividerViewHidden.assertValues(true)
    }

    @Test
    fun testProfileViewModel_EmitsCreatedProjectsData() {
        val user = UserFactory.user().toBuilder()
            .backedProjectsCount(0)
            .createdProjectsCount(2)
            .build()

        val apiClient = object : MockApiClient() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        // Backed text views are hidden.
        this.backedCountTextViewHidden.assertValues(true)
        this.backedCountTextViewText.assertNoValues()
        this.backedTextViewHidden.assertValues(true)

        // Created text views are displayed.
        this.createdCountTextViewHidden.assertValues(false)
        this.createdCountTextViewText.assertValues(NumberUtils.format(user.createdProjectsCount()!!))
        this.createdTextViewHidden.assertValues(false)

        // Divider view is hidden.
        this.dividerViewHidden.assertValues(true)
    }

    @Test
    fun testProfileViewModel_EmitsProjects() {
        val apiClient = object : MockApiClient() {
            override fun fetchProjects(params: DiscoveryParams): Observable<DiscoverEnvelope> {
                return Observable.just(
                    DiscoverEnvelopeFactory.discoverEnvelope(listOf(ProjectFactory.project()))
                )
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        this.projectList.assertValueCount(1)
    }

    @Test
    fun testProfileViewModel_EmitsUserNameAndAvatar() {
        val user = UserFactory.user()
        val apiClient = object : MockApiClient() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        this.avatarImageViewUrl.assertValues(user.avatar().medium())
        this.userNameTextViewText.assertValues(user.name())
    }

    @Test
    fun testProfileViewModel_ResumeDiscoveryActivity() {
        setUpEnvironment(environment())

        this.vm.inputs.exploreProjectsButtonClicked()
        this.resumeDiscoveryActivity.assertValueCount(1)
    }

    @Test
    fun testProfileViewModel_StartMessageThreadsActivity() {
        setUpEnvironment(environment())

        this.vm.inputs.messagesButtonClicked()
        this.startMessageThreadsActivity.assertValueCount(1)
    }

    @Test
    fun testProfileViewModel_StartProjectActivity() {
        setUpEnvironment(environment())

        val project = ProjectFactory.project()
        this.vm.inputs.projectCardClicked(project)
        this.startProjectActivity.assertValueCount(1)
        assertEquals(this.startProjectActivity.onNextEvents[0].first, project)
        assertFalse(this.startProjectActivity.onNextEvents[0].second)
        this.segmentTrack.assertValue(EventName.CARD_CLICKED.eventName)
    }

    @Test
    fun testProfileViewModel_whenFeatureFlagOn_shouldEmitProjectPage() {
        val user = MockCurrentUser()
        val mockExperimentsClientType: MockExperimentsClientType =
            object : MockExperimentsClientType() {
                override fun isFeatureEnabled(feature: OptimizelyFeature.Key): Boolean {
                    return true
                }
            }

        val environment = environment().toBuilder()
            .currentUser(user)
            .optimizely(mockExperimentsClientType)
            .build()

        setUpEnvironment(environment)

        val project = ProjectFactory.project()
        this.vm.inputs.projectCardClicked(project)
        this.startProjectActivity.assertValueCount(1)
        assertEquals(this.startProjectActivity.onNextEvents[0].first, project)
        assertTrue(this.startProjectActivity.onNextEvents[0].second)
        this.segmentTrack.assertValue(EventName.CARD_CLICKED.eventName)
    }
}
