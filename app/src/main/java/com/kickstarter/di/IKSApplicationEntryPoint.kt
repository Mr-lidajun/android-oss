package com.kickstarter.di

import com.google.gson.Gson
import com.kickstarter.libs.*
import com.kickstarter.services.ApiClientType
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@EntryPoint
interface IKSApplicationEntryPoint {
    fun environment(): EnvironmentImpl
    fun currentUser(): CurrentUserType
    fun apiClient(): ApiClientType
    fun currentConfig(): CurrentConfigType
    fun logOut(): LogoutDI
    fun build(): BuildDI
    fun ksString(): KSString
    fun font(): Font
    fun gSon(): Gson
}
