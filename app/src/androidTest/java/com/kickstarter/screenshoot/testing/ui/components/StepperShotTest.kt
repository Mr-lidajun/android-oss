package com.kickstarter.screenshoot.testing.ui.components
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.R
import com.kickstarter.ui.views.Stepper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class StepperShotTest : ScreenshotTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun stepperInitializationByDefaultTest() {
        val stepper = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_stepper, null
            ) as ConstraintLayout
            ).findViewById(R.id.stepper) as Stepper

        compareScreenshot(stepper)
    }

    @Test
    fun stepperInitializeMaxMinInitialValue() {
        val stepper = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_stepper, null
            ) as ConstraintLayout
            ).findViewById(R.id.stepper) as Stepper

        stepper.inputs.setMinimum(1)
        stepper.inputs.setMaximum(9)
        stepper.inputs.setInitialValue(5)

        compareScreenshot(stepper)
    }

    @Test
    fun stepperHitMax() {
        val stepper = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_stepper, null
            ) as ConstraintLayout
            ).findViewById(R.id.stepper) as Stepper

        stepper.inputs.setMinimum(1)
        stepper.inputs.setMaximum(9)
        stepper.inputs.setInitialValue(9)

        compareScreenshot(stepper)
    }

    @Test
    fun stepperHitMin() {
        val stepper = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_stepper, null
            ) as ConstraintLayout
            ).findViewById(R.id.stepper) as Stepper

        stepper.inputs.setMinimum(1)
        stepper.inputs.setMaximum(9)
        stepper.inputs.setInitialValue(1)

        compareScreenshot(stepper)
    }
}
