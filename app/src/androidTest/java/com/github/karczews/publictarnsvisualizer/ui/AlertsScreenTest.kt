package com.github.karczews.publictarnsvisualizer.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.karczews.publictarnsvisualizer.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AlertsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.onNodeWithText("Alerts").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun alertsScreen_showsTitle() {
        composeRule.onNodeWithText("Service Alerts").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_showsAlertHeader() {
        composeRule.onNodeWithText("Tram 10 detour on Piotrkowska").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_showsEffectLabel() {
        composeRule.onNodeWithText("DETOUR").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_showsAffectedRoute() {
        composeRule.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun alertCard_expandShowsDescription() {
        composeRule.onNodeWithText("Tram 10 detour on Piotrkowska").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Due to roadworks, tram 10 is rerouted via Kilińskiego.")
            .assertIsDisplayed()
    }
}
