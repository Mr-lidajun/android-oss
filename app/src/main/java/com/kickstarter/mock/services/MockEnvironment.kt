package com.kickstarter.mock.services

import com.kickstarter.libs.*
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.models.User
import rx.observers.TestSubscriber

class MockEnvironment : EnvironmentImpl {
    lateinit var experimentsTest: TestSubscriber<String>
    lateinit var segmentTrack: TestSubscriber<String>
    lateinit var segmentIdentify: TestSubscriber<User>

    override fun environment(): Environment {

        val mockCurrentConfig = MockCurrentConfig()
        val experimentsClientType = experimentsClient()
        val segmentTestClient = segmentTrackingClient(mockCurrentConfig, experimentsClientType)

        val config = ConfigFactory.config().toBuilder()
            .build()
        mockCurrentConfig.config(config)

        val mockShared = MockSharedPreferences()
        //val mockKSString = KSString(application.packageName, application.resources)
        //val mockCookieManager = Mockito.mock(CookieManager::class.java)

        return Environment.Builder()
            //.cookieManager(mockCookieManager)
            .sharedPreferences(mockShared)
            .ksCurrency(KSCurrency(mockCurrentConfig))
            .apiClient(MockApiClient())
            .apolloClient(MockApolloClient())
            .currentConfig(mockCurrentConfig)
            .currentUser(MockCurrentUser())
            .webClient(MockWebClient())
            //.ksString(mockKSString)
            //.stripe(Stripe(context(), Secrets.StripePublishableKey.STAGING))
            .analytics(AnalyticEvents(listOf(segmentTestClient)))
            .optimizely(experimentsClientType)
            .build()
    }

    private fun experimentsClient(): MockExperimentsClientType {
        experimentsTest = TestSubscriber()
        val experimentsClientType = MockExperimentsClientType()
        experimentsClientType.eventKeys.subscribe(experimentsTest)
        return experimentsClientType
    }

    private fun segmentTrackingClient(mockCurrentConfig: MockCurrentConfig, experimentsClientType: MockExperimentsClientType): MockTrackingClient {
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