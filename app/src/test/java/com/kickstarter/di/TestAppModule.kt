package com.kickstarter.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.apollographql.apollo.ApolloClient
import com.google.gson.Gson
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.Build
import com.kickstarter.libs.BuildCheck
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.DeviceRegistrarType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.Font
import com.kickstarter.libs.InternalToolsType
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.Logout
import com.kickstarter.libs.PushNotifications
import com.kickstarter.libs.SegmentTrackingClient
import com.kickstarter.libs.braze.RemotePushClientType
import com.kickstarter.libs.perimeterx.PerimeterXClientType
import com.kickstarter.libs.preferences.BooleanDataStoreType
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.preferences.IntPreferenceType
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.qualifiers.AccessTokenPreference
import com.kickstarter.libs.qualifiers.ActivitySamplePreference
import com.kickstarter.libs.qualifiers.ApiRetrofit
import com.kickstarter.libs.qualifiers.AppRatingPreference
import com.kickstarter.libs.qualifiers.ConfigPreference
import com.kickstarter.libs.qualifiers.FirstSessionPreference
import com.kickstarter.libs.qualifiers.GamesNewsletterPreference
import com.kickstarter.libs.qualifiers.PackageNameString
import com.kickstarter.libs.qualifiers.UserPreference
import com.kickstarter.libs.qualifiers.WebEndpoint
import com.kickstarter.libs.qualifiers.WebRetrofit
import com.kickstarter.libs.utils.PlayServicesCapability
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApiService
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.KSWebViewClient
import com.kickstarter.services.WebClientType
import com.kickstarter.services.WebService
import com.kickstarter.services.interceptors.ApiRequestInterceptor
import com.kickstarter.services.interceptors.GraphQLInterceptor
import com.kickstarter.services.interceptors.KSRequestInterceptor
import com.kickstarter.services.interceptors.WebRequestInterceptor
import com.stripe.android.Stripe
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.mockito.Mockito
import retrofit2.Retrofit
import rx.Scheduler
import java.net.CookieManager
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ApplicationModule::class]
)
object TestAppModule {

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
    fun provideAssetManager(@ApplicationContext appContext: Context): AssetManager {
        return appContext.assets
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext appContext: Context?): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext!!)
    }

    @Provides
    @Singleton
    fun provideStripe(@ApplicationContext context: Context, apiEndpoint: ApiEndpoint): Stripe {
        return Mockito.mock(Stripe::class.java)
    }

    @Provides
    @Singleton
    fun provideOptimizely(
        @ApplicationContext context: Context,
        apiEndpoint: ApiEndpoint,
        build: Build
    ): ExperimentsClientType {
        return Mockito.mock(ExperimentsClientType::class.java)
    }

    @Provides
    @Singleton
    fun provideEnvironment(
        @ActivitySamplePreference activitySamplePreference: IntPreferenceType,
        apiClient: ApiClientType,
        apolloClient: ApolloClientType,
        build: Build,
        buildCheck: BuildCheck,
        cookieManager: CookieManager,
        currentConfig: CurrentConfigType,
        currentUser: CurrentUserType,
        @FirstSessionPreference firstSessionPreference: BooleanPreferenceType,
        gson: Gson,
        @AppRatingPreference hasSeenAppRatingPreference: BooleanPreferenceType,
        @GamesNewsletterPreference hasSeenGamesNewsletterPreference: BooleanPreferenceType,
        internalToolsType: InternalToolsType,
        ksCurrency: KSCurrency,
        ksString: KSString,
        analytics: AnalyticEvents,
        logout: Logout,
        optimizely: ExperimentsClientType,
        playServicesCapability: PlayServicesCapability,
        scheduler: Scheduler,
        sharedPreferences: SharedPreferences,
        stripe: Stripe,
        webClient: WebClientType,
        @WebEndpoint webEndpoint: String
    ): Environment {
        return Environment.builder().build()
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
    fun provideApolloClient(
        build: Build,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        graphQLInterceptor: GraphQLInterceptor,
        @WebEndpoint webEndpoint: String,
        ksRequestInterceptor: KSRequestInterceptor
    ): ApolloClient {
        return Mockito.mock(ApolloClient::class.java)
    }

    @Provides
    @Singleton
    fun providePerimeterXManager(
        @ApplicationContext context: Context,
        build: Build
    ): PerimeterXClientType {
        return Mockito.mock(PerimeterXClientType::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiRequestInterceptor: ApiRequestInterceptor,
        cookieJar: CookieJar,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        ksRequestInterceptor: KSRequestInterceptor,
        build: Build,
        webRequestInterceptor: WebRequestInterceptor
    ): OkHttpClient {
        return Mockito.mock(OkHttpClient::class.java)
    }

    @Provides
    @Singleton
    @ApiRetrofit
    fun provideApiRetrofit(
        apiEndpoint: ApiEndpoint,
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Mockito.mock(Retrofit::class.java)
    }

    @Provides
    @Singleton
    fun provideApiRequestInterceptor(
        clientId: String,
        currentUser: CurrentUserType,
        endpoint: ApiEndpoint,
        manager: PerimeterXClientType,
        build: Build
    ): ApiRequestInterceptor {
        return Mockito.mock(ApiRequestInterceptor::class.java)
    }

    @Provides
    @Singleton
    fun provideGraphQLInterceptor(
        clientId: String,
        currentUser: CurrentUserType,
        build: Build,
        manager: PerimeterXClientType
    ): GraphQLInterceptor {
        return Mockito.mock(GraphQLInterceptor::class.java)
    }

    @Provides
    @Singleton
    fun provideApiService(@ApiRetrofit retrofit: Retrofit): ApiService {
        return Mockito.mock(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideClientId(apiEndpoint: ApiEndpoint): String {
        return if (apiEndpoint == ApiEndpoint.PRODUCTION) Secrets.Api.Client.PRODUCTION else Secrets.Api.Client.STAGING
    }

    @Provides
    @Singleton
    fun provideKSRequestInterceptor(
        build: Build,
        manager: PerimeterXClientType
    ): KSRequestInterceptor {
        return Mockito.mock(KSRequestInterceptor::class.java)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return Mockito.mock(HttpLoggingInterceptor::class.java)
    }

    @Provides
    @Singleton
    fun provideWebClientType(webService: WebService): WebClientType {
        return Mockito.mock(WebClientType::class.java)
    }

    @Provides
    @Singleton
    @WebRetrofit
    fun provideWebRetrofit(
        @WebEndpoint webEndpoint: String,
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Mockito.mock(Retrofit::class.java)
    }

    @Provides
    @Singleton
    fun provideWebRequestInterceptor(
        currentUser: CurrentUserType,
        @WebEndpoint endpoint: String,
        internalTools: InternalToolsType,
        build: Build,
        manager: PerimeterXClientType
    ): WebRequestInterceptor {
        return Mockito.mock(WebRequestInterceptor::class.java)
    }

    @Provides
    @Singleton
    fun provideWebService(@WebRetrofit retrofit: Retrofit): WebService {
        return Mockito.mock(WebService::class.java)
    }

    private fun createRetrofit(
        baseUrl: String,
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Mockito.mock(Retrofit::class.java)
    }

    @Provides
    @Singleton
    @AccessTokenPreference
    fun provideAccessTokenPreference(sharedPreferences: SharedPreferences): StringPreferenceType {
        return Mockito.mock(StringPreferenceType::class.java)
    }

    @Provides
    @Singleton
    fun providePlayServicesCapability(@ApplicationContext context: Context): PlayServicesCapability {
        return Mockito.mock(PlayServicesCapability::class.java)
    }

    @Provides
    @Singleton
    @ConfigPreference
    fun providesConfigPreference(sharedPreferences: SharedPreferences): StringPreferenceType {
        return Mockito.mock(StringPreferenceType::class.java)
    }

    @Provides
    @Singleton
    fun providesFeaturesFlagsPreference(sharedPreferences: SharedPreferences): StringPreferenceType {
        return Mockito.mock(StringPreferenceType::class.java)
    }

    @Provides
    @Singleton
    @ActivitySamplePreference
    fun provideActivitySamplePreference(sharedPreferences: SharedPreferences): IntPreferenceType {
        return Mockito.mock(IntPreferenceType::class.java)
    }

    @Provides
    @Singleton
    @AppRatingPreference
    fun provideAppRatingPreference(sharedPreferences: SharedPreferences): BooleanPreferenceType {
        return Mockito.mock(BooleanPreferenceType::class.java)
    }

    @Provides
    @Singleton
    @AppRatingPreference
    fun provideBooleanDataStoreType(@ApplicationContext context: Context): BooleanDataStoreType {
        return Mockito.mock(BooleanDataStoreType::class.java)
    }

    @Provides
    @Singleton
    @FirstSessionPreference
    fun provideFirstSessionPreference(sharedPreferences: SharedPreferences): BooleanPreferenceType {
        return Mockito.mock(BooleanPreferenceType::class.java)
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
    fun provideScheduler(): Scheduler {
        return Mockito.mock(Scheduler::class.java)
    }

    @Provides
    @Singleton
    fun provideBuild(packageInfo: PackageInfo): Build {
        return Build(packageInfo)
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
    fun provideCookieJar(cookieManager: CookieManager): CookieJar {
        return Mockito.mock(CookieJar::class.java)
    }

    @Provides
    @Singleton
    fun provideCookieManager(): CookieManager {
        return Mockito.mock(CookieManager::class.java)
    }

    @Provides
    @Singleton
    fun provideCurrentUser(
        @AccessTokenPreference accessTokenPreference: StringPreferenceType,
        deviceRegistrar: DeviceRegistrarType,
        gson: Gson,
        @UserPreference userPreference: StringPreferenceType
    ): CurrentUserType {
        return Mockito.mock(CurrentUserType::class.java)
    }

    @Provides
    @Singleton
    fun provideDeviceRegistrar(
        playServicesCapability: PlayServicesCapability,
        @ApplicationContext context: Context,
        brazeClient: RemotePushClientType
    ): DeviceRegistrarType {
        return Mockito.mock(DeviceRegistrarType::class.java)
    }

    @Provides
    @Singleton
    @GamesNewsletterPreference
    fun provideGamesNewsletterPreference(sharedPreferences: SharedPreferences): BooleanPreferenceType {
        return Mockito.mock(BooleanPreferenceType::class.java)
    }

    @Provides
    @Singleton
    @WebEndpoint
    fun provideWebEndpoint(apiEndpoint: ApiEndpoint): String {
        return if (apiEndpoint == ApiEndpoint.PRODUCTION) "https://www.kickstarter.com" else apiEndpoint.url()
            .replace("(?<=\\Ahttps?:\\/\\/)api.".toRegex(), "")
    }

    @Provides
    @Singleton
    fun provideFont(assetManager: AssetManager): Font {
        return Font(assetManager)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Mockito.mock(Gson::class.java)
    }

    @Provides
    @Singleton
    fun provideKSCurrency(currentConfig: CurrentConfigType): KSCurrency {
        return Mockito.mock(KSCurrency::class.java)
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
    fun provideKSWebViewClient(
        okHttpClient: OkHttpClient,
        @WebEndpoint webEndpoint: String?,
        manager: PerimeterXClientType
    ): KSWebViewClient {
        return Mockito.mock(KSWebViewClient::class.java)
    }

    @Provides
    @Singleton
    fun provideLogout(cookieManager: CookieManager, currentUser: CurrentUserType): Logout {
        return Mockito.mock(Logout::class.java)
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
    fun providePackageInfo(application: Application): PackageInfo {
        return try {
            application.packageManager.getPackageInfo(application.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            throw RuntimeException()
        }
    }

    @Provides
    @Singleton
    @PackageNameString
    fun providePackageName(application: Application): String {
        return application.packageName
    }

    @Provides
    @Singleton
    fun provideResources(@ApplicationContext context: Context): Resources {
        return context.resources
    }

    @Provides
    @Singleton
    @UserPreference
    fun provideUserPreference(sharedPreferences: SharedPreferences): StringPreferenceType {
        return Mockito.mock(StringPreferenceType::class.java)
    }
}
