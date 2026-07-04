package com.example.beforemealsignal.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.beforemealsignal.data.sampleDashboard
import com.example.beforemealsignal.theme.BeforeMealSignalTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** UI tests for [MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun onboardingCopy_exists() {
    composeTestRule.setContent {
      BeforeMealSignalTheme {
        MainScreen(state = MealSignalScreenState(dashboard = sampleDashboard, local = MealPrototypeState()))
      }
    }

    composeTestRule.onNodeWithText("반가워요!").assertIsDisplayed()
    composeTestRule.onNodeWithText("시작하기").assertIsDisplayed()
  }

  @Test
  fun homeCopy_exists() {
    composeTestRule.setContent {
      BeforeMealSignalTheme {
        MainScreen(
          state =
            MealSignalScreenState(
              dashboard = sampleDashboard,
              local = MealPrototypeState(showOnboarding = false),
            ),
        )
      }
    }

    composeTestRule.onNodeWithText("오늘 급식").assertIsDisplayed()
    assertTrue(composeTestRule.onAllNodesWithText("아침").fetchSemanticsNodes().isNotEmpty())
    assertTrue(composeTestRule.onAllNodesWithText("저녁").fetchSemanticsNodes().isNotEmpty())
    composeTestRule.onNodeWithText("잡곡밥").assertIsDisplayed()
  }

  @Test
  fun profileReminderSettings_exist() {
    composeTestRule.setContent {
      BeforeMealSignalTheme {
        MainScreen(
          state =
            MealSignalScreenState(
              dashboard = sampleDashboard,
              local = MealPrototypeState(showOnboarding = false, activeTab = MealTab.Profile),
            ),
        )
      }
    }

    composeTestRule.onNodeWithText("식전 알림").assertIsDisplayed()
    composeTestRule.onNodeWithText("아침·점심·저녁 10분 전").assertIsDisplayed()
    composeTestRule.onNodeWithText("20분 전").assertIsDisplayed()
  }
}
