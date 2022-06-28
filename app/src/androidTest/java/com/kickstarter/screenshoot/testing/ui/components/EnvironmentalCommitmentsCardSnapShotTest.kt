package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.R
import com.kickstarter.ui.views.EnvironmentalCommitmentsCard
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class EnvironmentalCommitmentsCardSnapShotTest : ScreenshotTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun layoutInitializationByDefaultTest() {
        val card = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_environmental_commitments_card, null
            ) as ConstraintLayout
            ).findViewById(R.id.environmentalCommitmentsCard) as EnvironmentalCommitmentsCard

        compareScreenshot(card)
    }
}
