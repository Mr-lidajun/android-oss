package com.kickstarter.screenshoot.testing
import com.kickstarter.KSApplication

class InstrumentedApp : KSApplication() {

    override fun onCreate() {
        super.onCreate()
    }

    override val isInUnitTests: Boolean
        get() = true
}
