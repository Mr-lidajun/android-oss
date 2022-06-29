package com.kickstarter.services.firebase

import android.content.Context
import android.os.Bundle
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.kickstarter.libs.Build
import com.kickstarter.libs.BuildDI
import com.kickstarter.libs.utils.extensions.build
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.IOException

class UnregisterTokenWorker(@ApplicationContext applicationContext: Context, private val params: WorkerParameters) : Worker(applicationContext, params) {
    lateinit var build: BuildDI

    override fun doWork(): Result {

        // TODO for now access directly to the SingletonComponent entry, but on next iterations will bring @HiltWorker to the picture
        build = applicationContext.build()

        return try {
            FirebaseMessaging.getInstance().deleteToken()
            logSuccess()
            Result.success()
        } catch (e: IOException) {
            logError(e)
            Result.failure()
        }
    }

    private fun logSuccess() {
        val successMessage = "Successfully unregistered push token"
        if (this.build.isDebug) {
            Timber.d(successMessage)
        }
        Firebase.analytics.logEvent("Successfully_unregistered_push_token", Bundle())
    }

    private fun logError(ioException: IOException) {
        if (this.build.isDebug) {
            Timber.e(ioException.localizedMessage)
        }
        FirebaseCrashlytics.getInstance().recordException(ioException)
    }
}
