package com.kickstarter.di

import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.Build
import com.kickstarter.libs.CookieManagerType
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.EnvironmentImpl
import com.kickstarter.libs.Font
import com.kickstarter.libs.KSString
import com.kickstarter.libs.Logout
import com.kickstarter.libs.PushNotifications
import com.kickstarter.libs.braze.RemotePushClientType
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.services.MockAnalytics
import com.kickstarter.mock.services.MockBuildDI
import com.kickstarter.mock.services.MockCookieManager
import com.kickstarter.mock.services.MockCurrentUser
import com.kickstarter.mock.services.MockEnvironment
import com.kickstarter.mock.services.MockFont
import com.kickstarter.mock.services.MockKSString
import com.kickstarter.mock.services.MockLogOut
import com.kickstarter.mock.services.MockPushNotifications
import com.kickstarter.mock.services.MockRemotePushClientType
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TestModule {

    @Binds
    abstract fun bindsEnvironment(
        impl: MockEnvironment
    ): EnvironmentImpl

    @Binds
    abstract fun bindsCurrentUser(
        impl: MockCurrentUser
    ): CurrentUserType

    @Binds
    abstract fun bindsCurrentConfig(
        impl: MockCurrentConfig
    ): CurrentConfigType

    @Binds
    abstract fun bindsLogOut(
        impl: MockLogOut
    ): Logout

    @Binds
    abstract fun bindsBuild(
        impl: MockBuildDI
    ): Build

    @Binds
    abstract fun bindsKSString(
        impl: MockKSString
    ): KSString

    @Binds
    abstract fun bindsFont(
        impl: MockFont
    ): Font

    @Binds
    abstract fun bindsCookieManager(
        impl: MockCookieManager
    ): CookieManagerType

    @Binds
    abstract fun bindsPushNotification(
        impl: MockPushNotifications
    ): PushNotifications

    @Binds
    abstract fun bindsRemotePushClientType(
        impl: MockRemotePushClientType
    ): RemotePushClientType

    @Binds
    abstract fun bindsAnalyticsEvents(
        impl: MockAnalytics
    ): AnalyticEvents
}
