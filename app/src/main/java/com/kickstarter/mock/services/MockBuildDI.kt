package com.kickstarter.mock.services

import com.kickstarter.libs.BuildDI
import org.joda.time.DateTime

class MockBuildDI:BuildDI {
    override val isDebug: Boolean
        get() = true
    override val isRelease: Boolean
        get() = false

    override fun applicationId(): String {
        return "ID"
    }

    override fun buildDate(): DateTime {
        return DateTime()
    }

    override fun sha(): String {
        return "sha"
    }

    override fun versionCode(): Int {
        return 0
    }

    override fun versionName(): String {
        return "versionName"
    }

    override fun variant(): String {
        return "Test"
    }

    override fun isExternal(): Boolean {
        return false
    }

    override fun isInternal(): Boolean {
        return false
    }

}