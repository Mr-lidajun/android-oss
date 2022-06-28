package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class RiskMessageShotTest : ScreenshotTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun layoutInitializationByDefaultTest() {
        val layout = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.fragment_checkout_risk_message, null
            ) as ConstraintLayout
            ).findViewById(R.id.risk_message_cl) as ConstraintLayout

        compareScreenshot(layout)
    }
}
