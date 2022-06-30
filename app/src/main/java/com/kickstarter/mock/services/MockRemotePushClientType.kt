package com.kickstarter.mock.services

import android.app.Application
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import com.kickstarter.libs.braze.RemotePushClientType

class MockRemotePushClientType(override val isInitialized: Boolean) : RemotePushClientType {
    override fun init() {
        TODO("Not yet implemented")
    }

    override fun getIdSender(): String {
        TODO("Not yet implemented")
    }

    override fun registerPushMessages(context: Context, token: String) {
        TODO("Not yet implemented")
    }

    override fun handleRemoteMessages(context: Context, message: RemoteMessage): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLifeCycleCallbacks(): Application.ActivityLifecycleCallbacks {
        TODO("Not yet implemented")
    }

    override fun isSDKEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun registerActivityLifecycleCallbacks(context: Context) {
        TODO("Not yet implemented")
    }
}
