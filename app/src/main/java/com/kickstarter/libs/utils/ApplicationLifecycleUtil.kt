package com.kickstarter.libs.utils

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.kickstarter.KSApplication
import com.kickstarter.libs.Config
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Logout
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.apiClient
import com.kickstarter.libs.utils.extensions.config
import com.kickstarter.libs.utils.extensions.currentUser
import com.kickstarter.libs.utils.extensions.logOut
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.fromThrowable
import rx.Notification

class ApplicationLifecycleUtil(private val application: KSApplication) :
    ActivityLifecycleCallbacks,
    ComponentCallbacks2 {

    private val client: ApiClientType = application.apiClient()
    private val config: CurrentConfigType = application.config()
    private val currentUser: CurrentUserType = application.currentUser()
    private val logout: Logout = application.logOut()
    private var isInBackground = true
    private var isLoggedIn = false

    init {
        currentUser.isLoggedIn.subscribe { userLoggedIn: Boolean -> isLoggedIn = userLoggedIn }
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        if (isInBackground) {
            // Facebook: logs 'install' and 'app activate' App Events.
            AppEventsLogger.activateApp(activity.application)
            refreshConfigFile()
            refreshUser()
            isInBackground = false
        }
    }

    /**
     * Refresh the config file.
     */
    private fun refreshConfigFile() {
        client.config()
            .materialize()
            .share()
            .subscribe { notification: Notification<Config> ->
                if (notification.hasValue()) {
                    config.config(notification.value)
                }
                if (notification.hasThrowable()) {
                    handleConfigApiError(fromThrowable(notification.throwable)!!)
                }
            }
    }

    /**
     * Handles a config API error by logging the user out in the case of a 401. We will interpret
     * 401's on the config request as meaning the user's current access token is no longer valid,
     * as that endpoint should never 401 othewise.
     */
    private fun handleConfigApiError(error: ErrorEnvelope) {
        if (error.httpCode() == 401) {
            forceLogout("config_api_error")
        }
    }

    /**
     * Forces the current user session to be logged out.
     */
    private fun forceLogout(context: String) {
        logout.execute()
        ApplicationUtils.startNewDiscoveryActivity(application)
        val params = Bundle()
        params.putString("location", context)
        FirebaseAnalytics.getInstance(application).logEvent("force_logout", params)
    }

    /**
     * Refreshes the user object if there is not a user logged in with a non-null access token.
     */
    private fun refreshUser() {
        val accessToken = currentUser.accessToken

        // Check if the access token is null and the user is still logged in.
        if (isLoggedIn && ObjectUtils.isNull(accessToken)) {
            forceLogout("access_token_null")
        } else {
            if (ObjectUtils.isNotNull(accessToken)) {
                client.fetchCurrentUser()
                    .compose(Transformers.neverError())
                    .subscribe { u: User? ->
                        currentUser.refresh(
                            u!!
                        )
                    }
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onConfigurationChanged(configuration: Configuration) {}
    override fun onLowMemory() {}

    /**
     * Memory availability callback. TRIM_MEMORY_UI_HIDDEN means the app's UI is no longer visible.
     * This is triggered when the user navigates out of the app and primarily used to free resources used by the UI.
     * http://developer.android.com/training/articles/memory.html
     */
    override fun onTrimMemory(i: Int) {
        if (i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            isInBackground = true
        }
    }
}
