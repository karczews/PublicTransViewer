package com.github.karczews.publictarnsvisualizer.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.karczews.publictarnsvisualizer.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

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
