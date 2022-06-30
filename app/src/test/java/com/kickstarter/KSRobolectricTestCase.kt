package com.kickstarter

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.kickstarter.di.ApplicationModule
import com.kickstarter.libs.*
import com.kickstarter.libs.braze.RemotePushClientType
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.qualifiers.AccessTokenPreference
import com.kickstarter.libs.qualifiers.ConfigPreference
import com.kickstarter.libs.qualifiers.PackageNameString
import com.kickstarter.libs.qualifiers.UserPreference
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.mock.services.MockWebClient
import com.kickstarter.models.User
import com.kickstarter.services.ApiClient
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApiService
import com.stripe.android.Stripe
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import junit.framework.TestCase
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import java.net.CookieManager
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(KSRobolectricGradleTestRunner::class)
@HiltAndroidTest
@UninstallModules(ApplicationModule::class)
@Config(shadows = [ShadowAndroidXMultiDex::class], sdk = [KSRobolectricGradleTestRunner.DEFAULT_SDK], application = HiltTestApplication::class)
abstract class KSRobolectricTestCase : TestCase() {
    val application: Application = ApplicationProvider.getApplicationContext()


    lateinit var experimentsTest: TestSubscriber<String>
    lateinit var segmentTrack: TestSubscriber<String>
    lateinit var segmentIdentify: TestSubscriber<User>

    val mockCurrentConfig = MockCurrentConfig()
    val experimentsClientType = experimentsClient()
    val segmentTestClient = segmentTrackingClient(mockCurrentConfig, experimentsClientType)

    private lateinit var environment: Environment

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var environmentInjected: Environment

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        hiltAndroidRule.inject()

        val config = ConfigFactory.config().toBuilder()
            .build()

        mockCurrentConfig.config(config)

        environment = environmentInjected.toBuilder()
            .ksCurrency(KSCurrency(mockCurrentConfig))
            .apiClient(MockApiClient())
            .apolloClient(MockApolloClient())
            .currentConfig(mockCurrentConfig)
            .webClient(MockWebClient())
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

    @Module
    @InstallIn(SingletonComponent::class) // 2
    object TestAppModule {

        @Provides
        fun provideEnvironment(): Environment { // 3
            val mockCurrentConfig = MockCurrentConfig()
            val config = ConfigFactory.config().toBuilder()
                .build()
            mockCurrentConfig.config(config)

            return Environment.builder()
                .apiClient(MockApiClient())
                .apolloClient(MockApolloClient())
                .currentConfig(mockCurrentConfig)
                .webClient(MockWebClient())
                .build()
        }

        @Provides
        @Singleton
        fun provideCurrentUser(
            @AccessTokenPreference accessTokenPreference: StringPreferenceType,
            deviceRegistrar: DeviceRegistrarType,
            gson: Gson,
            @UserPreference userPreference: StringPreferenceType
        ): CurrentUserType {
            return MockCurrentUser()
        }

        @Provides
        @Singleton
        fun provideCurrentConfig(
            assetManager: AssetManager,
            gson: Gson,
            @ConfigPreference configPreference: StringPreferenceType
        ): CurrentConfigType {
            return MockCurrentConfig()
        }

        @Provides
        @Singleton
        fun provideLogout(cookieManager: CookieManager, currentUser: CurrentUserType): Logout {
            return Mockito.mock(Logout::class.java)
        }

        @Provides
        @Singleton
        fun provideBuild(packageInfo: PackageInfo): Build {
            return Mockito.mock(Build::class.java)
        }

        @Provides
        @Singleton
        fun provideKSString(
            @PackageNameString packageName: String,
            resources: Resources
        ): KSString {
            return Mockito.mock(KSString::class.java)
        }

        @Provides
        @Singleton
        fun provideFont(assetManager: AssetManager): Font {
            return Mockito.mock(Font::class.java)
        }

        @Provides
        @Singleton
        fun provideGson(): Gson {
            return Mockito.mock(Gson::class.java)
        }

        @Provides
        @Singleton
        fun provideCookieManager(): CookieManager {
            return Mockito.mock(CookieManager::class.java)
        }

        @Provides
        @Singleton
        fun providePushNotifications(
            @ApplicationContext context: Context,
            client: ApiClientType,
            experimentsClientType: ExperimentsClientType
        ): PushNotifications {
            return Mockito.mock(PushNotifications::class.java)
        }

        @Provides
        @Singleton
        fun provideBrazeClient(
            build: Build,
            @ApplicationContext context: Context
        ): RemotePushClientType {
            return Mockito.mock(RemotePushClientType::class.java)
        }

        @Provides
        @Singleton
        fun provideAnalytics(
            segmentClient: SegmentTrackingClient
        ): AnalyticEvents {
            return Mockito.mock(AnalyticEvents::class.java)
        }

        @Provides
        @Singleton
        fun provideSegmentTrackingClient(
            @ApplicationContext context: Context,
            currentUser: CurrentUserType,
            build: Build,
            currentConfig: CurrentConfigType,
            experimentsClientType: ExperimentsClientType
        ): SegmentTrackingClient {
            return Mockito.mock(SegmentTrackingClient::class.java)
        }

        @Provides
        @Singleton
        fun provideOptimizely(
            @ApplicationContext context: Context,
            apiEndpoint: ApiEndpoint,
            build: Build
        ): ExperimentsClientType {
            return MockExperimentsClientType()
        }
    }
}
