package com.kickstarter

import com.facebook.FacebookSdk.sdkInitialize

class TestKSApplication : KSApplication() {
    override fun onCreate() {
        // - LoginToutViewModelTest needs the FacebookSDK initialized
        sdkInitialize(this)
        super.onCreate()
    }

    override val isInUnitTests: Boolean
        get() = true
}
