package com.kickstarter.libs

import android.content.pm.PackageInfo
import com.kickstarter.BuildConfig
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.lang.StringBuilder

interface BuildDI {
    val isDebug: Boolean
    val isRelease: Boolean
    fun applicationId(): String
    fun buildDate(): DateTime
    fun sha(): String
    fun versionCode(): Int
    fun versionName(): String
    fun variant(): String
    fun isExternal(): Boolean
    fun isInternal(): Boolean
}
class Build(private val packageInfo: PackageInfo): BuildDI {
    override fun applicationId(): String {
        return packageInfo.packageName
    }

    override fun buildDate(): DateTime {
        return DateTime(
            BuildConfig.BUILD_DATE,
            DateTimeZone.UTC
        ).withZone(DateTimeZone.getDefault())
    }

    /**
     * Returns `true` if the build is compiled in debug mode, `false` otherwise.
     */
    override val isDebug: Boolean
        get() = BuildConfig.DEBUG

    /**
     * Returns `true` if the build is compiled in release mode, `false` otherwise.
     */
    override val isRelease: Boolean
        get() = !BuildConfig.DEBUG

    override fun sha(): String {
        return BuildConfig.GIT_SHA
    }

    override fun versionCode(): Int {
        return packageInfo.versionCode
    }

    override fun versionName(): String {
        return packageInfo.versionName
    }

    override fun variant(): String {
        // e.g. internalDebug, externalRelease
        return StringBuilder().append(BuildConfig.FLAVOR)
            .append(BuildConfig.BUILD_TYPE.substring(0, 1).uppercase())
            .append(BuildConfig.BUILD_TYPE.substring(1))
            .toString()
    }

    override fun isInternal() = isInternal
    override fun isExternal() = isExternal

    companion object {
        val isInternal: Boolean
            get() = BuildConfig.FLAVOR == "internal"
        val isExternal: Boolean
            get() = !isInternal
    }
}