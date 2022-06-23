package com.kickstarter.services.firebase

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.libs.Build
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.utils.extensions.apiClient
import com.kickstarter.libs.utils.extensions.build
import com.kickstarter.services.ApiClientType
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.IOException

class ResetDeviceIdWorker(@ApplicationContext applicationContext: Context, params: WorkerParameters) : Worker(applicationContext, params) {
    lateinit var apiClient: ApiClientType
    lateinit var build: Build

    override fun doWork(): Result {
        // TODO for now access directly to the SingletonComponent entry, but on next iterations will bring @HiltWorker to the picture
        apiClient = applicationContext.apiClient()
        build = applicationContext.build()

        return try {
            FirebaseHelper.delete()
            logSuccess()
            applicationContext.sendBroadcast(Intent(BROADCAST))
            Result.success()
        } catch (e: IOException) {
            logError(e)
            Result.failure()
        }
    }

    private fun logSuccess() {
        val successMessage = "Successfully reset Firebase device ID"
        if (this.build.isDebug) {
            Timber.d(successMessage)
        }
        FirebaseCrashlytics.getInstance().log(successMessage)
    }

    private fun logError(ioException: IOException) {
        if (this.build.isDebug) {
            Timber.e(ioException.localizedMessage)
        }
        FirebaseCrashlytics.getInstance().recordException(ioException)
    }

    companion object {
        const val BROADCAST = "reset_device_id_success"
        const val TAG = "ResetDeviceIdWorker"
    }
}
