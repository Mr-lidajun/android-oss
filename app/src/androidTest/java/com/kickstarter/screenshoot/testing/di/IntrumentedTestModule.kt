package com.kickstarter.screenshoot.testing.di

import com.kickstarter.libs.*
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.services.MockBuildDI
import com.kickstarter.mock.services.MockCurrentUser
import com.kickstarter.mock.services.MockEnvironment
import com.kickstarter.mock.services.MockLogOut
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ActivityModule {

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
    ): LogoutDI

    @Binds
    fun provideBuild(
        impl: MockBuildDI
    ): BuildDI
}