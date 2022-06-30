package com.kickstarter.di

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
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.Build
import com.kickstarter.libs.BuildCheck
import com.kickstarter.libs.BuildImpl
import com.kickstarter.libs.CurrentConfig
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUser
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.DateTimeTypeConverter
import com.kickstarter.libs.DeviceRegistrar
import com.kickstarter.libs.DeviceRegistrarType
import com.kickstarter.libs.EXPERIMENTS_CLIENT_READY
import com.kickstarter.libs.Environment.Companion.builder
import com.kickstarter.libs.EnvironmentImpl
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.Font
import com.kickstarter.libs.FontImpl
import com.kickstarter.libs.InternalToolsType
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.KSStringImpl
import com.kickstarter.libs.Logout
import com.kickstarter.libs.LogoutImpl
import com.kickstarter.libs.OptimizelyExperimentsClient
import com.kickstarter.libs.PushNotifications
import com.kickstarter.libs.PushNotificationsImpl
import com.kickstarter.libs.SegmentTrackingClient
import com.kickstarter.libs.braze.BrazeClient
import com.kickstarter.libs.braze.RemotePushClientType
import com.kickstarter.libs.graphql.DateAdapter
import com.kickstarter.libs.graphql.DateTimeAdapter
import com.kickstarter.libs.graphql.EmailAdapter
import com.kickstarter.libs.graphql.Iso8601DateTimeAdapter
import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.libs.perimeterx.PerimeterXClient
import com.kickstarter.libs.perimeterx.PerimeterXClientType
import com.kickstarter.libs.preferences.BooleanDataStore
import com.kickstarter.libs.preferences.BooleanDataStoreType
import com.kickstarter.libs.preferences.BooleanPreference
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.preferences.IntPreference
import com.kickstarter.libs.preferences.IntPreferenceType
import com.kickstarter.libs.preferences.StringPreference
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
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApiService
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.KSWebViewClient
import com.kickstarter.services.WebClient
import com.kickstarter.services.WebClientType
import com.kickstarter.services.WebService
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
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Scheduler
import rx.schedulers.Schedulers
import timber.log.Timber
import type.CustomType
import java.net.CookieManager
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApplicationModule {

    @Provides
    @Singleton
    fun provideSegmentTrackingClient(
        @ApplicationContext context: Context,
        currentUser: CurrentUserType,
        build: Build,
        currentConfig: CurrentConfigType,
        experimentsClientType: ExperimentsClientType
    ): SegmentTrackingClient {
        return SegmentTrackingClient(
            build,
            context,
            currentConfig,
            currentUser,
            experimentsClientType,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
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
        val stripePublishableKey =
            if (apiEndpoint == ApiEndpoint.PRODUCTION) Secrets.StripePublishableKey.PRODUCTION else Secrets.StripePublishableKey.STAGING
        PaymentConfiguration.init(
            context,
            stripePublishableKey
        )
        return Stripe(context, stripePublishableKey)
    }

    @Provides
    @Singleton
    fun provideOptimizely(
        @ApplicationContext context: Context,
        apiEndpoint: ApiEndpoint,
        build: Build
    ): ExperimentsClientType {
        val optimizelyEnvironment: OptimizelyEnvironment = when (apiEndpoint) {
            ApiEndpoint.PRODUCTION -> {
                OptimizelyEnvironment.PRODUCTION
            }
            ApiEndpoint.STAGING -> {
                OptimizelyEnvironment.STAGING
            }
            else -> {
                OptimizelyEnvironment.DEVELOPMENT
            }
        }

        val optimizelyManager = OptimizelyManager.builder()
            .withSDKKey(optimizelyEnvironment.sdkKey)
            .withDatafileDownloadInterval(15, TimeUnit.MINUTES)
            .withEventDispatchInterval(2L, TimeUnit.MILLISECONDS)
            .build(context)
        optimizelyManager.initialize(context, null) { optimizely: OptimizelyClient ->
            if (!optimizely.isValid) {
                FirebaseCrashlytics.getInstance()
                    .recordException(Throwable("Optimizely failed to initialize."))
            } else {
                if (build.isDebug) {
                    Timber.d(
                        ApplicationModule::class.java.simpleName,
                        "🔮 Optimizely successfully initialized."
                    )
                }
                context.sendBroadcast(Intent(EXPERIMENTS_CLIENT_READY))
            }
        }
        return OptimizelyExperimentsClient(optimizelyManager, optimizelyEnvironment)
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
    ): EnvironmentImpl {
        return builder()
            .activitySamplePreference(activitySamplePreference)
            .apiClient(apiClient)
            .apolloClient(apolloClient)
            .build(build)
            .buildCheck(buildCheck)
            .cookieManager(cookieManager)
            .currentConfig(currentConfig)
            .currentUser(currentUser)
            .firstSessionPreference(firstSessionPreference)
            .gson(gson)
            .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
            .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
            .internalTools(internalToolsType)
            .ksCurrency(ksCurrency)
            .ksString(ksString)
            .analytics(analytics)
            .logout(logout)
            .optimizely(optimizely)
            .playServicesCapability(playServicesCapability)
            .scheduler(scheduler)
            .sharedPreferences(sharedPreferences)
            .stripe(stripe)
            .webClient(webClient)
            .webEndpoint(webEndpoint)
            .build()
    }

    @Provides
    @Singleton
    fun provideBrazeClient(
        build: Build,
        @ApplicationContext context: Context
    ): RemotePushClientType {
        return BrazeClient(context, build)
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
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(graphQLInterceptor)
            .addInterceptor(ksRequestInterceptor)

        // Only log in debug mode to avoid leaking sensitive information.
        if (build.isDebug) {
            builder.addInterceptor(httpLoggingInterceptor)
        }
        val okHttpClient: OkHttpClient = builder.build()
        return ApolloClient.builder()
            .addCustomTypeAdapter(CustomType.DATE, DateAdapter())
            .addCustomTypeAdapter(CustomType.EMAIL, EmailAdapter())
            .addCustomTypeAdapter(CustomType.ISO8601DATETIME, Iso8601DateTimeAdapter())
            .addCustomTypeAdapter(CustomType.DATETIME, DateTimeAdapter())
            .serverUrl("$webEndpoint/graph")
            .okHttpClient(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun providePerimeterXManager(
        @ApplicationContext context: Context,
        build: Build
    ): PerimeterXClientType {
        val manager = PerimeterXClient(build)
        if (context is KSApplication && !context.isInUnitTests) {
            manager.start(context)
        }
        return manager
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
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()

        // Only log in debug mode to avoid leaking sensitive information.
        if (build.isDebug) {
            builder.addInterceptor(httpLoggingInterceptor)
        }
        return builder
            .addInterceptor(apiRequestInterceptor)
            .addInterceptor(webRequestInterceptor)
            .addInterceptor(ksRequestInterceptor)
            .cookieJar(cookieJar)
            .build()
    }

    @Provides
    @Singleton
    @ApiRetrofit
    fun provideApiRetrofit(
        apiEndpoint: ApiEndpoint,
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return createRetrofit(apiEndpoint.url(), gson, okHttpClient)
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
        return ApiRequestInterceptor(clientId, currentUser, endpoint.url(), manager, build)
    }

    @Provides
    @Singleton
    fun provideGraphQLInterceptor(
        clientId: String,
        currentUser: CurrentUserType,
        build: Build,
        manager: PerimeterXClientType
    ): GraphQLInterceptor {
        return GraphQLInterceptor(clientId, currentUser, build, manager)
    }

    @Provides
    @Singleton
    fun provideApiService(@ApiRetrofit retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
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
        return KSRequestInterceptor(build, manager)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
        return interceptor
    }

    @Provides
    @Singleton
    fun provideWebClientType(webService: WebService): WebClientType {
        return WebClient(webService)
    }

    @Provides
    @Singleton
    @WebRetrofit
    fun provideWebRetrofit(
        @WebEndpoint webEndpoint: String,
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return createRetrofit(webEndpoint, gson, okHttpClient)
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
        return WebRequestInterceptor(currentUser, endpoint, internalTools, build, manager)
    }

    @Provides
    @Singleton
    fun provideWebService(@WebRetrofit retrofit: Retrofit): WebService {
        return retrofit.create(WebService::class.java)
    }

    private fun createRetrofit(
        baseUrl: String,
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @AccessTokenPreference
    fun provideAccessTokenPreference(sharedPreferences: SharedPreferences): StringPreferenceType {
        return StringPreference(sharedPreferences, SharedPreferenceKey.ACCESS_TOKEN)
    }

    @Provides
    @Singleton
    fun providePlayServicesCapability(@ApplicationContext context: Context): PlayServicesCapability {
        return PlayServicesCapability(context)
    }

    @Provides
    @Singleton
    @ConfigPreference
    fun providesConfigPreference(sharedPreferences: SharedPreferences): StringPreferenceType {
        return StringPreference(sharedPreferences, SharedPreferenceKey.CONFIG)
    }

    @Provides
    @Singleton
    fun providesFeaturesFlagsPreference(sharedPreferences: SharedPreferences): StringPreferenceType {
        return StringPreference(sharedPreferences, SharedPreferenceKey.FEATURE_FLAG)
    }

    @Provides
    @Singleton
    @ActivitySamplePreference
    fun provideActivitySamplePreference(sharedPreferences: SharedPreferences): IntPreferenceType {
        return IntPreference(sharedPreferences, SharedPreferenceKey.LAST_SEEN_ACTIVITY_ID)
    }

    @Provides
    @Singleton
    @AppRatingPreference
    fun provideAppRatingPreference(sharedPreferences: SharedPreferences): BooleanPreferenceType {
        return BooleanPreference(sharedPreferences, SharedPreferenceKey.HAS_SEEN_APP_RATING)
    }

    @Provides
    @Singleton
    @AppRatingPreference
    fun provideBooleanDataStoreType(@ApplicationContext context: Context): BooleanDataStoreType {
        return BooleanDataStore(context, SharedPreferenceKey.FIRST_SESSION)
    }

    @Provides
    @Singleton
    @FirstSessionPreference
    fun provideFirstSessionPreference(sharedPreferences: SharedPreferences): BooleanPreferenceType {
        return BooleanPreference(sharedPreferences, SharedPreferenceKey.FIRST_SESSION)
    }

    @Provides
    @Singleton
    fun provideAnalytics(
        segmentClient: SegmentTrackingClient
    ): AnalyticEvents {
        val clients = listOf(segmentClient)
        return AnalyticEvents(clients)
    }

    @Provides
    @Singleton
    fun provideScheduler(): Scheduler {
        return Schedulers.computation()
    }

    @Provides
    @Singleton
    fun provideBuild(packageInfo: PackageInfo): Build {
        return BuildImpl(packageInfo)
    }

    @Provides
    @Singleton
    fun provideCurrentConfig(
        assetManager: AssetManager,
        gson: Gson,
        @ConfigPreference configPreference: StringPreferenceType
    ): CurrentConfigType {
        return CurrentConfig(assetManager, gson, configPreference)
    }

    @Provides
    @Singleton
    fun provideCookieJar(cookieManager: CookieManager): CookieJar {
        return JavaNetCookieJar(cookieManager)
    }

    @Provides
    @Singleton
    fun provideCookieManager(): CookieManager {
        return CookieManager()
    }

    @Provides
    @Singleton
    fun provideCurrentUser(
        @AccessTokenPreference accessTokenPreference: StringPreferenceType,
        deviceRegistrar: DeviceRegistrarType,
        gson: Gson,
        @UserPreference userPreference: StringPreferenceType
    ): CurrentUserType {
        return CurrentUser(accessTokenPreference, deviceRegistrar, gson, userPreference)
    }

    @Provides
    @Singleton
    fun provideDeviceRegistrar(
        playServicesCapability: PlayServicesCapability,
        @ApplicationContext context: Context,
        brazeClient: RemotePushClientType
    ): DeviceRegistrarType {
        return DeviceRegistrar(playServicesCapability, context, brazeClient)
    }

    @Provides
    @Singleton
    @GamesNewsletterPreference
    fun provideGamesNewsletterPreference(sharedPreferences: SharedPreferences): BooleanPreferenceType {
        return BooleanPreference(
            sharedPreferences,
            SharedPreferenceKey.HAS_SEEN_GAMES_NEWSLETTER
        )
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
        return FontImpl(assetManager)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(DateTime::class.java, DateTimeTypeConverter())
            .create()
    }

    @Provides
    @Singleton
    fun provideKSCurrency(currentConfig: CurrentConfigType): KSCurrency {
        return KSCurrency(currentConfig)
    }

    @Provides
    @Singleton
    fun provideKSString(
        @PackageNameString packageName: String,
        resources: Resources
    ): KSString {
        return KSStringImpl(packageName, resources)
    }

    @Provides
    fun provideKSWebViewClient(
        okHttpClient: OkHttpClient,
        @WebEndpoint webEndpoint: String?,
        manager: PerimeterXClientType
    ): KSWebViewClient {
        return KSWebViewClient(okHttpClient, webEndpoint!!, manager)
    }

    @Provides
    @Singleton
    fun provideLogout(cookieManager: CookieManager, currentUser: CurrentUserType): Logout {
        return LogoutImpl(cookieManager, currentUser)
    }

    @Provides
    @Singleton
    fun providePushNotifications(
        @ApplicationContext context: Context,
        client: ApiClientType
    ): PushNotifications {
        return PushNotificationsImpl(context, client)
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
        return StringPreference(sharedPreferences, SharedPreferenceKey.USER)
    }
}
