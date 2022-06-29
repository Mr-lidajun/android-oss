package com.kickstarter

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.TrackingClientType
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.mock.services.MockCurrentUser
import com.kickstarter.mock.services.MockTrackingClient
import com.kickstarter.mock.services.MockWebClient
import com.kickstarter.models.User
import com.stripe.android.Stripe
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import java.net.CookieManager

@RunWith(KSRobolectricGradleTestRunner::class)
@Config(shadows = [ShadowAndroidXMultiDex::class], sdk = [KSRobolectricGradleTestRunner.DEFAULT_SDK], application = HiltTestApplication::class)
abstract class KSRobolectricTestCase : TestCase() {

    // - Previously obtained via DaggerComponent.component().environment()
    lateinit var environment: Environment

    private val application: Application = ApplicationProvider.getApplicationContext()

    lateinit var experimentsTest: TestSubscriber<String>
    lateinit var segmentTrack: TestSubscriber<String>
    lateinit var segmentIdentify: TestSubscriber<User>

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()

        val mockCurrentConfig = MockCurrentConfig()
        val experimentsClientType = experimentsClient()
        val segmentTestClient = segmentTrackingClient(mockCurrentConfig, experimentsClientType)

        val config = ConfigFactory.config().toBuilder()
            .build()
        mockCurrentConfig.config(config)

        val mockShared = PreferenceManager.getDefaultSharedPreferences(application)
        val mockKSString = KSString(application.packageName, application.resources)
        val mockCookieManager = Mockito.mock(CookieManager::class.java)

        environment = Environment.Builder()
            .cookieManager(mockCookieManager)
            .sharedPreferences(mockShared)
            .ksCurrency(KSCurrency(mockCurrentConfig))
            .apiClient(MockApiClient())
            .apolloClient(MockApolloClient())
            .currentConfig(mockCurrentConfig)
            .currentUser(MockCurrentUser())
            .webClient(MockWebClient())
            .ksString(mockKSString)
            .stripe(Stripe(context(), Secrets.StripePublishableKey.STAGING))
            .analytics(AnalyticEvents(listOf(segmentTestClient)))
            .optimizely(experimentsClientType)
            .build()
    }

    protected fun application() = this.application

    @After
    @Throws(Exception::class)
    public override fun tearDown() {
        super.tearDown()
        DateTimeUtils.setCurrentMillisSystem()
    }

    protected fun context(): Context = this.application().applicationContext

    protected fun environment() = environment

    protected fun ksString() = KSString(application().packageName, application().resources)

    private fun experimentsClient(): MockExperimentsClientType {
        experimentsTest = TestSubscriber()
        val experimentsClientType = MockExperimentsClientType()
        experimentsClientType.eventKeys.subscribe(experimentsTest)
        return experimentsClientType
    }

    private fun segmentTrackingClient(mockCurrentConfig: MockCurrentConfig, experimentsClientType: MockExperimentsClientType): MockTrackingClient {
        segmentTrack = TestSubscriber()
        segmentIdentify = TestSubscriber()
        val segmentTrackingClient =
            MockTrackingClient(
                MockCurrentUser(),
                mockCurrentConfig, TrackingClientType.Type.SEGMENT, experimentsClientType
            )
        segmentTrackingClient.eventNames.subscribe(segmentTrack)
        segmentTrackingClient.identifiedUser.subscribe(segmentIdentify)
        return segmentTrackingClient
    }
}
