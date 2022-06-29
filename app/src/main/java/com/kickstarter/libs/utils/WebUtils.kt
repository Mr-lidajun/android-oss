package com.kickstarter.libs.utils

import com.kickstarter.libs.BuildDI

object WebUtils {
    fun userAgent(build: BuildDI): String {
        return StringBuilder()
            .append("Kickstarter Android Mobile Variant/")
            .append(build.variant())
            .append(" Code/")
            .append(build.versionCode())
            .append(" Version/")
            .append(build.versionName())
            .toString()
    }
}
