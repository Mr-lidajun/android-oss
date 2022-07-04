package com.kickstarter

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockTrackingClient
import com.kickstarter.libs.TrackingClientType
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.mock.services.MockWebClient
import com.kickstarter.models.User
import com.stripe.android.Stripe
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import javax.inject.Inject

@RunWith(KSRobolectricGradleTestRunner::class)
@Config(shadows = [ShadowAndroidXMultiDex::class], sdk = [KSRobolectricGradleTestRunner.DEFAULT_SDK], application = HiltTestApplication::class)
abstract class KSRobolectricTestCase : TestCase() {
    val application: Application = ApplicationProvider.getApplicationContext()

    lateinit var experimentsTest: TestSubscriber<String>
    lateinit var segmentTrack: TestSubscriber<String>
    lateinit var segmentIdentify: TestSubscriber<User>

    val mockCurrentConfig = MockCurrentConfig()
    val experimentsClientType = experimentsClient()
    private val segmentTestClient = segmentTrackingClient(mockCurrentConfig, experimentsClientType)

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var environmentInjected: Environment

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        hiltAndroidRule.inject()
        super.setUp()

        val config = ConfigFactory.config().toBuilder()
            .build()

        mockCurrentConfig.config(config)
    }

    protected fun application() = this.application

    @After
    @Throws(Exception::class)
    public override fun tearDown() {
        super.tearDown()
        DateTimeUtils.setCurrentMillisSystem()
    }

    protected fun context(): Context = this.application().applicationContext

    protected fun environment() = environmentInjected.toBuilder()
        .currentUser(MockCurrentUser())
        .ksCurrency(KSCurrency(mockCurrentConfig))
        .apiClient(MockApiClient())
        .apolloClient(MockApolloClient())
        .currentConfig(mockCurrentConfig)
        .webClient(MockWebClient())
        .stripe(Stripe(context(), Secrets.StripePublishableKey.STAGING))
        .analytics(AnalyticEvents(listOf(segmentTestClient)))
        .optimizely(experimentsClientType)
        .build()

    protected fun ksString() = KSString(application().packageName, application().resources)

    fun experimentsClient(): MockExperimentsClientType {
        experimentsTest = TestSubscriber()
        val experimentsClientType = MockExperimentsClientType()
        experimentsClientType.eventKeys.subscribe(experimentsTest)
        return experimentsClientType
    }

    fun segmentTrackingClient(mockCurrentConfig: MockCurrentConfig, experimentsClientType: MockExperimentsClientType): MockTrackingClient {
        segmentTrack = TestSubscriber()
        segmentIdentify = TestSubscriber()
        val segmentTrackingClient = MockTrackingClient(
            MockCurrentUser(),
            mockCurrentConfig, TrackingClientType.Type.SEGMENT, experimentsClientType
        )
        segmentTrackingClient.eventNames.subscribe(segmentTrack)
        segmentTrackingClient.identifiedUser.subscribe(segmentIdentify)
        return segmentTrackingClient
    }
}
