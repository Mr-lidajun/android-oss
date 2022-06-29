package com.kickstarter.services.firebase

import android.content.Context
import android.os.Bundle
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.kickstarter.libs.Build
import com.kickstarter.libs.DateTimeTypeConverter
import com.kickstarter.libs.utils.extensions.apiClient
import com.kickstarter.libs.utils.extensions.build
import com.kickstarter.libs.utils.extensions.isZero
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import dagger.hilt.android.qualifiers.ApplicationContext
import org.joda.time.DateTime
import rx.schedulers.Schedulers
import timber.log.Timber

class RegisterTokenWorker(@ApplicationContext applicationContext: Context, private val params: WorkerParameters) : Worker(applicationContext, params) {

    lateinit var apiClient: ApiClientType
    lateinit var build: Build

    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(DateTime::class.java, DateTimeTypeConverter())
        .create()

    private val token = this.params.inputData.getString(IntentKey.PUSH_TOKEN) as String

    override fun doWork(): Result {
        // TODO for now access directly to the SingletonComponent entry, but on next iterations will bring @HiltWorker to the picture
        apiClient = applicationContext.apiClient()
        build = applicationContext.build()

        return handleResponse(
            this.apiClient
                .registerPushToken(this.token)
                .subscribeOn(Schedulers.io())
                .toBlocking()
                .first()
        )
    }

    private fun handleResponse(response: JsonObject): Result {
        return if (response.size().isZero()) {
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL)
            logResponse()
            Result.success()
        } else {
            Result.retry()
            try {
                val errorEnvelope = this.gson.fromJson(response, ErrorEnvelope::class.java)
                logError("📵 Failed to register push token ${errorEnvelope.httpCode()} ${errorEnvelope.errorMessages()?.firstOrNull()}")
                when (errorEnvelope.httpCode()) {
                    in 400..499 -> Result.failure()
                    else -> Result.retry()
                }
            } catch (exception: JsonSyntaxException) {
                logError("📵 Failed to deserialize push token error $response")
                Result.failure()
            }
        }
    }

    private fun logResponse() {
        val successMessage = "📲 Successfully registered push token"
        if (this.build.isDebug) {
            Timber.d(successMessage)
        }
        Firebase.analytics.logEvent("Successfully_registered_push_token", Bundle())
    }

    private fun logError(errorMessage: String) {
        if (this.build.isDebug) {
            Timber.e(errorMessage)
        }
        FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
    }

    companion object {
        private const val TOPIC_GLOBAL = "global"
    }
}
