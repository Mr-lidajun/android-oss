package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.R
import com.kickstarter.ui.views.MediaHeader
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MediaHeaderShotTest : ScreenshotTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    lateinit var mediaHeader: MediaHeader

    @Before
    fun setup() {

        mediaHeader = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_media_header, null
            ) as ConstraintLayout
            ).findViewById(R.id.media_header)

        mediaHeader.inputs.setProjectPhoto(null)
    }

    @Test
    fun playButton_whenVisibilityFalse_isGone() {
        mediaHeader.inputs.setPlayButtonVisibility(false)

        compareScreenshot(mediaHeader)
    }

    @Test
    fun playButton_whenVisibilityTrue_isVisible() {
        mediaHeader.inputs.setPlayButtonVisibility(true)

        compareScreenshot(mediaHeader)
    }
}
