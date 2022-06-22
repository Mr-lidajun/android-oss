package com.kickstarter.di

import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.Font
import com.kickstarter.libs.KSString
import com.kickstarter.libs.Logout
import com.kickstarter.services.ApiClientType
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@EntryPoint
interface IKSApplicationEntryPoint {
    fun environment(): Environment
    fun currentUser(): CurrentUserType
    fun apiClient(): ApiClientType
    fun currentConfig(): CurrentConfigType
    fun logOut(): Logout
    fun build(): Build
    fun ksString(): KSString
    fun font(): Font
}
