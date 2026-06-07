package com.github.karczews.publictarnsvisualizer.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.karczews.publictarnsvisualizer.MainActivity
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNavigation_showsAllTabs() {
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Stops").assertIsDisplayed()
        composeRule.onNodeWithText("Alerts").assertIsDisplayed()
    }

    @Test
    fun navigateToStops_showsSearchField() {
        composeRule.onNodeWithText("Stops").performClick()
        composeRule.onNodeWithTag("search_field").assertIsDisplayed()
    }

    @Test
    fun navigateToAlerts_showsTitle() {
        composeRule.onNodeWithText("Alerts").performClick()
        composeRule.onNodeWithText("Service Alerts").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsRecenterButton() {
        composeRule.onNodeWithTag("recenter_button").assertIsDisplayed()
    }
}
