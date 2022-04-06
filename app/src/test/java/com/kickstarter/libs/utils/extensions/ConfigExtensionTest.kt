package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.ConfigFactory
import org.json.JSONArray
import org.junit.Test
import java.util.Collections

class ConfigExtensionTest : KSRobolectricTestCase() {

    @Test
    fun isFeatureFlagEnabled_whenFeatureFlagTrue_returnTrue() {
        val config = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair(R.string.kickstarter_icon.toString(), true)))
        assertTrue(config.isFeatureFlagEnabled(R.string.kickstarter_icon.toString()))
    }

    @Test
    fun isFeatureFlagEnabled_whenFeatureFlagFalse_returnFalse() {
        val config = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair(R.string.kickstarter_icon.toString(), false)))
        assertFalse(config.isFeatureFlagEnabled(R.string.kickstarter_icon.toString()))
    }

    @Test
    fun isEnabledFeature_whenFeatureFlagEmpty_returnFalse() {
        val config = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair("", true)))
        assertFalse(config.isFeatureFlagEnabled(R.string.kickstarter_icon.toString()))
    }

    @Test
    fun testAbExperiments() {
        val configNull = ConfigFactory.config().toBuilder().abExperiments(null).build()
        assertEquals(null, configNull.currentVariants())
    }

    @Test
    fun currentVariants_whenExperimentsEmpty_currentVariantsShouldBeEmpty() {
        val configEmpty = ConfigFactory.configWithExperiments(Collections.emptyMap())
        assertEquals(0, configEmpty.currentVariants()?.size)
    }

    @Test
    fun currentVariants_whenGivenOneExperiment_shouldReturnOneCurrentVariant() {
        val configWithExperiment = ConfigFactory.configWithExperiment("pledge_button_copy", "experiment")
        assertEquals(1, configWithExperiment.currentVariants()?.size)
        assertEquals("pledge_button_copy[experiment]", configWithExperiment.currentVariants()?.get(0))
    }

    @Test
    fun currentVariants_whenGivenMapOfExperiments_shouldReturnCorrectNumberOfCurrentVariants() {
        val configWithExperiments = ConfigFactory.configWithExperiments(
            mapOf(
                Pair("pledge_button_copy", "experiment"),
                Pair("add_new_card_vertical", "control")
            )
        )

        assertEquals(2, configWithExperiments.currentVariants()?.size)
        assertEquals("add_new_card_vertical[control]", configWithExperiments.currentVariants()?.get(0))
        assertEquals("pledge_button_copy[experiment]", configWithExperiments.currentVariants()?.get(1))
    }

    @Test
    fun enabledFeatureFlags_whenFeaturesNull_enabledFeatureFlagsShouldBeNull() {
        val configEmptyFeatureFlags = ConfigFactory.config().toBuilder().features(null).build()
        assertEquals(null, configEmptyFeatureFlags.enabledFeatureFlags())
    }

    @Test
    fun enabledFeatureFlags_whenGiveOneFeatureFlag_shouldReturnOneEnabledFeatureFlag() {
        val configOneFeatureFlag = ConfigFactory.configWithFeatureEnabled("ios_native_checkout")
        assertEquals(JSONArray(), configOneFeatureFlag.enabledFeatureFlags())
    }

    @Test
    fun enabledFeatureFlags_whenGivenMapOfFeatureFlags_shouldReturnCorrectNumberOfEnabledFeatureFlags() {
        val configSeveralFeatureFlags = ConfigFactory.configWithFeaturesEnabled(
            mapOf(
                Pair("android_native_checkout", false),
                Pair("ios_go_rewardless", true),
                Pair("ios_native_checkout", true)
            )
        )

        assertEquals(JSONArray(), configSeveralFeatureFlags.enabledFeatureFlags())
    }

    @Test
    fun enabledFeatureFlags_whenGivenMapOfFeatureFlagsDisabled_shouldReturnCorrectNumberOfEnabledFeatureFlags() {
        val configSeveralFeatureFlags = ConfigFactory.configWithFeatureDisabled("android_native_checkout")

        assertEquals(JSONArray(), configSeveralFeatureFlags.enabledFeatureFlags())
    }

    @Test
    fun enabledFeatureFlags_whenGivenTrueFeatureFlag_shouldReturnTrueEnabledFeatureFlag() {
        val configEnableFeatureFlag = ConfigFactory.configWithFeaturesEnabled(
            mapOf(
                Pair("android_native_checkout", true),
                Pair("ios_go_rewardless", true),
                Pair("ios_native_checkout", true)
            )
        )

        assertEquals(
            JSONArray().apply {
                put("android_native_checkout")
            },
            configEnableFeatureFlag.enabledFeatureFlags()
        )
    }

    @Test
    fun setUserFeatureFlagsPrefWithFeatureFlag_whenGivenTrueFeatureFlag_shouldReturnTrueEnabledFeatureFlag() {
        val configEnableFeatureFlag = ConfigFactory.configWithFeaturesEnabled(
            mapOf(
                Pair("android_native_checkout", false),
            )
        )

        val newConfig = configEnableFeatureFlag
            .setUserFeatureFlagsPrefWithFeatureFlag(null, "android_native_checkout", true)

        val flag = newConfig.features()?.get("android_native_checkout") ?: false

        assertTrue(flag)
    }
}
