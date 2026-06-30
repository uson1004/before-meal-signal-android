package com.example.beforemealsignal.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.beforemealsignal.data.DietProfile
import com.example.beforemealsignal.data.FoodSignal
import com.example.beforemealsignal.data.FoodSignalDashboard
import com.example.beforemealsignal.data.QuickAction
import com.example.beforemealsignal.data.RiskLevel
import com.example.beforemealsignal.data.SignalTone
import com.example.beforemealsignal.theme.BeforeMealSignalTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      BeforeMealSignalTheme { MainScreen(state = fakeState, onQueryChange = {}, onFoodSelected = {}) }
    }
  }

  @Test
  fun coreCopy_exists() {
    composeTestRule.onNodeWithText("먹기 전 신호").assertExists()
    composeTestRule.onNodeWithText("섭취 전 확인 필요").assertExists()
    composeTestRule.onNodeWithText("직원에게 보여줄 문장").assertExists()
  }
}

private val fakeFood =
  FoodSignal(
    name = "테스트 김밥",
    brand = "테스트 브랜드",
    category = "가공식품",
    source = "공식 데이터",
    updatedAt = "2026.06.30",
    confidence = "공식",
    headline = "섭취 전 확인 필요",
    summary = "우유 포함 가능성이 있습니다.",
    riskLevel = RiskLevel.High,
    matchedAllergens = listOf("우유"),
    possibleAllergens = listOf("대두"),
    spicyLevel = 0,
    cautionTags = listOf("주문 전 확인"),
    recommendedAction = "성분표를 확인하세요.",
  )

private val fakeState =
  FoodSignalScreenState(
    dashboard =
      FoodSignalDashboard(
        profile = DietProfile(allergens = listOf("우유"), dietTags = listOf("매운맛 약함"), spicyTolerance = 1),
        quickActions = listOf(QuickAction("검색", "제품명으로 확인", SignalTone.Info)),
        foods = listOf(fakeFood),
        staffPrompt = "우유 포함 여부를 확인해 주세요.",
      ),
    query = "",
    filteredFoods = listOf(fakeFood),
    selectedFood = fakeFood,
  )
