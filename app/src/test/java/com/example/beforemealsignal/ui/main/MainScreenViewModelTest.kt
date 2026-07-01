package com.example.beforemealsignal.ui.main

import com.example.beforemealsignal.data.DataRepository
import com.example.beforemealsignal.data.MealSignalDashboard
import com.example.beforemealsignal.data.sampleDashboard
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {
  @Test
  fun uiState_startsWithOnboardingDefaults() = runTest {
    val viewModel = MainScreenViewModel(FakeMealRepository())

    val state = viewModel.successState()

    assertTrue(state.local.showOnboarding)
    assertEquals(setOf("계란", "우유"), state.selectedAllergens)
    assertEquals(1, state.spicyTolerance)
  }

  @Test
  fun onStartOnboarding_showsHomeWithTodayRisk() = runTest {
    val viewModel = MainScreenViewModel(FakeMealRepository())

    viewModel.onStartOnboarding()
    val state = viewModel.successState()

    assertFalse(state.local.showOnboarding)
    assertEquals(MealTab.Home, state.local.activeTab)
    assertEquals(listOf("계란"), state.todayMatchedAllergens)
    assertEquals("계란찜", state.riskMenuName)
  }

  @Test
  fun onSubmitReport_incrementsReportAndStreakCounts() = runTest {
    val viewModel = MainScreenViewModel(FakeMealRepository())

    val before = viewModel.successState()
    viewModel.onSubmitReport()
    val after = viewModel.successState { it.local.reportSubmitted }

    assertTrue(after.local.reportSubmitted)
    assertEquals(before.reportCount + 1, after.reportCount)
    assertEquals(before.streakDays + 1, after.streakDays)
  }
}

private suspend fun MainScreenViewModel.successState(
  predicate: (MealSignalScreenState) -> Boolean = { true },
): MealSignalScreenState =
  (uiState.first { it is MainScreenUiState.Success && predicate(it.state) } as MainScreenUiState.Success).state

private class FakeMealRepository : DataRepository {
  override val dashboard: Flow<MealSignalDashboard> = flowOf(sampleDashboard)
}
