package com.kickstarter

import android.text.TextUtils
import androidx.annotation.CallSuper
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.CookieManagerType
import com.kickstarter.libs.FirebaseHelper.Companion.identifier
import com.kickstarter.libs.FirebaseHelper.Companion.initialize
import com.kickstarter.libs.PushNotifications
import com.kickstarter.libs.braze.RemotePushClientType
import com.kickstarter.libs.utils.ApplicationLifecycleUtil
import com.kickstarter.libs.utils.Secrets
import dagger.hilt.android.HiltAndroidApp
import org.joda.time.DateTime
import timber.log.Timber
import timber.log.Timber.Forest.plant
import java.net.CookieHandler
import java.net.HttpCookie
import java.net.URI
import java.util.*
import javax.inject.Inject

@HiltAndroidApp
open class KSApplication : MultiDexApplication() {

    @Inject
    lateinit var pushNotifications: PushNotifications

    @Inject
    lateinit var remotePushClientType: RemotePushClientType

    @Inject
    lateinit var analytics: AnalyticEvents

    @Inject
    lateinit var cookieManagerType: CookieManagerType

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        if (!isInUnitTests) {
            initApplication()
        }
    }

    private fun initApplication() {
        MultiDex.install(this)

        // Only log for internal builds
        if (BuildConfig.FLAVOR == "internal") {
            plant(Timber.DebugTree())
        }
        initialize(applicationContext) { initializeDependencies() }
    }

    // - Returns Boolean because incompatible Java "void" type with kotlin "Void" type for the lambda declaration
    private fun initializeDependencies(): Boolean {
        setVisitorCookie()
        pushNotifications.initialize()
        val appUtil = ApplicationLifecycleUtil(this)
        registerActivityLifecycleCallbacks(appUtil)
        registerComponentCallbacks(appUtil)

        // - Initialize Segment SDK
        if (analytics != null) {
            analytics.initialize()
        }

        // - Register lifecycle callback for Braze
        remotePushClientType.registerActivityLifecycleCallbacks(this)
        return true
    }

    /**
     * Method override in tha child class for testings purposes
     */
    open val isInUnitTests: Boolean
        get() = false

    private fun setVisitorCookie() {
        val cookieManager = cookieManagerType.manager()
        val deviceId = identifier
        val uniqueIdentifier =
            if (TextUtils.isEmpty(deviceId)) UUID.randomUUID().toString() else deviceId
        val cookie = HttpCookie("vis", uniqueIdentifier)
        cookie.maxAge = DateTime.now().plusYears(100).millis
        cookie.secure = true
        val webUri = URI.create(Secrets.WebEndpoint.PRODUCTION)
        val apiUri = URI.create(ApiEndpoint.PRODUCTION.url())
        cookieManager.cookieStore?.add(webUri, cookie)
        cookieManager.cookieStore?.add(apiUri, cookie)
        CookieHandler.setDefault(cookieManager)
    }
}
