package com.kickstarter.viewmodels

import UpdateUserCurrencyMutation
import UserPrivacyQuery
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.apollographql.apollo.ApolloClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kickstarter.KSApplication
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.di.ApplicationModule
import com.kickstarter.di.InternalApplicationModule
import com.kickstarter.libs.*
import com.kickstarter.libs.braze.BrazeClient
import com.kickstarter.libs.braze.RemotePushClientType
import com.kickstarter.libs.graphql.DateAdapter
import com.kickstarter.libs.graphql.DateTimeAdapter
import com.kickstarter.libs.graphql.EmailAdapter
import com.kickstarter.libs.graphql.Iso8601DateTimeAdapter
import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.libs.perimeterx.PerimeterXClient
import com.kickstarter.libs.perimeterx.PerimeterXClientType
import com.kickstarter.libs.preferences.*
import com.kickstarter.libs.qualifiers.*
import com.kickstarter.libs.utils.PlayServicesCapability
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.mock.services.MockWebClient
import com.kickstarter.services.*
import com.kickstarter.services.interceptors.ApiRequestInterceptor
import com.kickstarter.services.interceptors.GraphQLInterceptor
import com.kickstarter.services.interceptors.KSRequestInterceptor
import com.kickstarter.services.interceptors.WebRequestInterceptor
import com.kickstarter.ui.SharedPreferenceKey
import com.optimizely.ab.android.sdk.OptimizelyClient
import com.optimizely.ab.android.sdk.OptimizelyManager
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Scheduler
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import timber.log.Timber
import type.CurrencyCode
import type.CustomType
import java.net.CookieManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(ApplicationModule::class)
class AccountViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: AccountViewModel.ViewModel

    private val chosenCurrency = TestSubscriber<String>()
    private val email = TestSubscriber<String>()
    private val error = TestSubscriber<String>()
    private val passwordRequiredContainerIsVisible = TestSubscriber<Boolean>()
    private val showEmailErrorIcon = TestSubscriber<Boolean>()
    private val success = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = AccountViewModel.ViewModel(environment)

        this.vm.outputs.chosenCurrency().subscribe(this.chosenCurrency)
        this.vm.outputs.email().subscribe(this.email)
        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.passwordRequiredContainerIsVisible().subscribe(this.passwordRequiredContainerIsVisible)
        this.vm.outputs.showEmailErrorIcon().subscribe(this.showEmailErrorIcon)
        this.vm.outputs.success().subscribe(this.success)
    }

    @Test
    fun testUserCurrency() {
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", true, true, true, true, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.chosenCurrency.assertValue("MXN")
        this.showEmailErrorIcon.assertValue(false)
    }

    @Test
    fun testUserEmail() {
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "r@ksr.com", true, true, true, true, "USD"
                            )
                        )
                    )
                }
            }).build()
        )

        this.email.assertValue("r@ksr.com")
        this.showEmailErrorIcon.assertValue(false)
    }

    @Test
    fun testChosenCurrencyMutation() {
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
                    return Observable.just(
                        UpdateUserCurrencyMutation.Data(
                            UpdateUserCurrencyMutation
                                .UpdateUserProfile("", UpdateUserCurrencyMutation.User("", currency.rawValue()))
                        )
                    )
                }
            }).build()
        )

        this.chosenCurrency.assertValue("USD")
        this.vm.inputs.onSelectedCurrency(CurrencyCode.AUD)
        this.chosenCurrency.assertValues("USD", CurrencyCode.AUD.rawValue())
        this.success.assertValue(CurrencyCode.AUD.rawValue())
        this.vm.inputs.onSelectedCurrency(CurrencyCode.AUD)
        this.chosenCurrency.assertValues("USD", CurrencyCode.AUD.rawValue())
    }

    @Test
    fun testShowEmailErrorIcon() {
        val hasPassword = true
        val isCreator = true
        val isDeliverable = false
        val isEmailVerified = true
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.showEmailErrorIcon.assertValue(true)
    }

    @Test
    fun testShowEmailErrorIconForBackerUndeliverable() {
        val hasPassword = true
        val isCreator = false
        val isDeliverable = false
        val isEmailVerified = false
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.showEmailErrorIcon.assertValue(true)
    }

    @Test
    fun testShowEmailErrorIconGoneForBackerUnverified() {
        val hasPassword = true
        val isCreator = false
        val isDeliverable = true
        val isEmailVerified = true
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.showEmailErrorIcon.assertValue(false)
    }

    @Test
    fun testPasswordRequiredContainerIsVisible_hasPassword() {
        val hasPassword = true
        val isCreator = false
        val isDeliverable = true
        val isEmailVerified = false
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.passwordRequiredContainerIsVisible.assertValue(true)
    }

    @Test
    fun testPasswordRequiredContainerIsVisible_noPassword() {
        val hasPassword = false
        val isCreator = false
        val isDeliverable = true
        val isEmailVerified = false
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.passwordRequiredContainerIsVisible.assertValue(false)
    }

    @Test
    fun testShowEmailErrorIconGoneForBackerDeliverable() {
        val hasPassword = true
        val isCreator = false
        val isDeliverable = true
        val isEmailVerified = false
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.showEmailErrorIcon.assertValue(false)
    }

    @Test
    fun testShowEmailErrorIconForCreatorUnverified() {
        val hasPassword = true
        val isCreator = true
        val isDeliverable = false
        val isEmailVerified = false
        setUpEnvironment(
            environment().toBuilder().apolloClient(object : MockApolloClient() {
                override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                    return Observable.just(
                        UserPrivacyQuery.Data(
                            UserPrivacyQuery.Me(
                                "", "",
                                "", hasPassword, isCreator, isDeliverable, isEmailVerified, "MXN"
                            )
                        )
                    )
                }
            }).build()
        )

        this.showEmailErrorIcon.assertValue(true)
    }
}
