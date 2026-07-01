package com.example.beforemealsignal.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.beforemealsignal.data.sampleDashboard
import com.example.beforemealsignal.theme.BeforeMealSignalTheme
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

    composeTestRule.onNodeWithText("오늘 급식 먼저 확인해요").assertIsDisplayed()
    composeTestRule.onNodeWithText("오늘 급식").assertIsDisplayed()
    composeTestRule.onNodeWithText("아침").assertIsDisplayed()
    composeTestRule.onNodeWithText("저녁").assertIsDisplayed()
    composeTestRule.onNodeWithText("잡곡밥").assertIsDisplayed()
    composeTestRule.onNodeWithText("빠른 체크인").assertIsDisplayed()
  }
}
