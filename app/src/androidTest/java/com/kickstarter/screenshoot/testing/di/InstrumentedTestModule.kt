package com.kickstarter.screenshoot.testing.di

import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.EnvironmentImpl
import com.kickstarter.libs.Font
import com.kickstarter.libs.KSString
import com.kickstarter.libs.Logout
import com.kickstarter.libs.PushNotifications
import com.kickstarter.libs.braze.RemotePushClientType
import com.kickstarter.mock.MockCurrentConfig
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
import java.net.CookieManager

@Module
@InstallIn(SingletonComponent::class)
interface InstrumentedTestModule {

    @Binds
    fun provideEnvironment(
        impl: MockEnvironment
    ): EnvironmentImpl

    @Binds
    fun provideCurrentUser(
        impl: MockCurrentUser
    ): CurrentUserType

    @Binds
    fun provideCurrentConfig(
        impl: MockCurrentConfig
    ): CurrentConfigType

    @Binds
    fun provideLogOut(
        impl: MockLogOut
    ): Logout

    @Binds
    fun provideBuild(
        impl: MockBuildDI
    ): Build

    @Binds
    fun provideKSString(
        impl: MockKSString
    ): KSString

    @Binds
    fun provideFont(
        impl: MockFont
    ): Font

    @Binds
    fun provideCookieManager(
        impl: MockCookieManager
    ): CookieManager

    @Binds
    fun providePushNotification(
        impl: MockPushNotifications
    ): PushNotifications

    @Binds
    fun provideRemotePushClientType(
        impl: MockRemotePushClientType
    ): RemotePushClientType
}
