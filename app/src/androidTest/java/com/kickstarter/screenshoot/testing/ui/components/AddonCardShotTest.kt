package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.R
import com.kickstarter.di.ApplicationModule
import com.kickstarter.mock.factories.RewardsItemFactory
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.views.AddOnCard
import com.kickstarter.ui.views.Stepper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(ApplicationModule::class)
class AddonCardShotTest : ScreenshotTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var addonCard: AddOnCard
    private lateinit var stepper: Stepper
    var itemsAdapter: RewardItemsAdapter = RewardItemsAdapter()

    @Test
    fun allViewsVisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun descriptionInvisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(false)
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun dividerInvisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(false)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun conversionInvisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(false)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun backerLimitInvisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(false)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun quantityRemainingInvisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(false)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun timeLeftInvisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(false)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun shippingAmountInvisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(false)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun stepperVisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
        stepper.inputs.setInitialValue(1)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun itemListInvisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(false)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(emptyList())
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun localPickupIsVisible() {
        addonCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_add_on_pledge, null
            ) as CardView
            )
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(false)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setLocalPickUpIsGone(false)
        addonCard.setLocalPickUpName("Lo Angeles, CA")
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(emptyList())
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }
}
