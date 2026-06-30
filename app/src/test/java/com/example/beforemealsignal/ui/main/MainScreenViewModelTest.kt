package com.example.beforemealsignal.ui.main

import com.example.beforemealsignal.data.DataRepository
import com.example.beforemealsignal.data.DietProfile
import com.example.beforemealsignal.data.FoodSignal
import com.example.beforemealsignal.data.FoodSignalDashboard
import com.example.beforemealsignal.data.QuickAction
import com.example.beforemealsignal.data.RiskLevel
import com.example.beforemealsignal.data.SignalTone
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {
  @Test
  fun uiState_exposesFirstFoodAsSelected() = runTest {
    val viewModel = MainScreenViewModel(FakeSignalRepository())

    val state = viewModel.uiState.first { it is MainScreenUiState.Success } as MainScreenUiState.Success

    assertEquals("테스트 김밥", state.state.selectedFood?.name)
    assertEquals(1, state.state.filteredFoods.size)
  }

  @Test
  fun onQueryChange_filtersFoodsByAllergen() = runTest {
    val viewModel = MainScreenViewModel(FakeSignalRepository())

    viewModel.onQueryChange("우유")
    val state = viewModel.uiState.first { it is MainScreenUiState.Success } as MainScreenUiState.Success

    assertTrue(state.state.filteredFoods.all { it.possibleAllergens.contains("우유") || it.matchedAllergens.contains("우유") })
  }
}

private class FakeSignalRepository : DataRepository {
  override val dashboard: Flow<FoodSignalDashboard> =
    flowOf(
      FoodSignalDashboard(
        profile = DietProfile(allergens = listOf("우유"), dietTags = listOf("매운맛 약함"), spicyTolerance = 1),
        quickActions = listOf(QuickAction("검색", "제품명으로 확인", SignalTone.Info)),
        foods =
          listOf(
            FoodSignal(
              name = "테스트 김밥",
              brand = "테스트 브랜드",
              category = "가공식품",
              source = "공식 데이터",
              updatedAt = "2026.06.30",
              confidence = "공식",
              headline = "확인 필요",
              summary = "우유 포함 가능성이 있습니다.",
              riskLevel = RiskLevel.Check,
              matchedAllergens = emptyList(),
              possibleAllergens = listOf("우유"),
              spicyLevel = 0,
              cautionTags = listOf("주문 전 확인"),
              recommendedAction = "성분표를 확인하세요.",
            )
          ),
        staffPrompt = "우유 포함 여부를 확인해 주세요.",
      )
    )
}
