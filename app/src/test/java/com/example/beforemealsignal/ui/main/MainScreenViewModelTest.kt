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
    assertTrue(state.local.notificationsEnabled)
    assertEquals("아침·점심·저녁 10분 전", state.reminderSummary)
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

  @Test
  fun onNotificationsToggle_updatesReminderSummary() = runTest {
    val viewModel = MainScreenViewModel(FakeMealRepository())

    viewModel.onNotificationsToggle()
    val state = viewModel.successState { !it.local.notificationsEnabled }

    assertEquals("알림 꺼짐", state.reminderSummary)
  }

  @Test
  fun onReminderMealToggled_keepsAtLeastOneMealPeriod() = runTest {
    val viewModel = MainScreenViewModel(FakeMealRepository())

    viewModel.onReminderMealToggled("아침")
    viewModel.onReminderMealToggled("점심")
    viewModel.onReminderMealToggled("저녁")
    val state = viewModel.successState { it.local.reminderSettings.mealPeriods == setOf("저녁") }

    assertEquals(setOf("저녁"), state.local.reminderSettings.mealPeriods)
    assertEquals("저녁 10분 전", state.reminderSummary)
  }

  @Test
  fun onReminderLeadSelected_acceptsOnlySupportedLeadTimes() = runTest {
    val viewModel = MainScreenViewModel(FakeMealRepository())

    viewModel.onReminderLeadSelected(20)
    viewModel.onReminderLeadSelected(5)
    val state = viewModel.successState { it.local.reminderSettings.leadMinutes == 20 }

    assertEquals(20, state.local.reminderSettings.leadMinutes)
    assertEquals("아침·점심·저녁 20분 전", state.reminderSummary)
  }

  @Test
  fun reminderSummary_usesOnlyServedMealPeriods() = runTest {
    val lunchOnlyDashboard =
      sampleDashboard.copy(
        weekMeals =
          sampleDashboard.weekMeals.map { meal ->
            if (!meal.isToday) {
              meal
            } else {
              meal.copy(
                mealSections =
                  meal.mealSections.map { section ->
                    if (section.displayName == "점심") section else section.copy(menuItems = emptyList())
                  },
              )
            }
          },
      )
    val viewModel = MainScreenViewModel(FakeMealRepository(lunchOnlyDashboard))

    val state = viewModel.successState()

    assertEquals("점심 10분 전", state.reminderSummary)
  }

  @Test
  fun onReminderMealToggled_ignoresUnknownMealPeriod() = runTest {
    val viewModel = MainScreenViewModel(FakeMealRepository())

    viewModel.onReminderMealToggled("야식")
    val state = viewModel.successState()

    assertEquals(setOf("아침", "점심", "저녁"), state.local.reminderSettings.mealPeriods)
    assertEquals("아침·점심·저녁 10분 전", state.reminderSummary)
  }

  @Test
  fun uiState_handlesEmptyWeekMeals() = runTest {
    val viewModel = MainScreenViewModel(FakeMealRepository(sampleDashboard.copy(weekMeals = emptyList())))

    val state = viewModel.successState()

    assertTrue(state.todayMeal.menuItems.isEmpty())
    assertEquals("오늘 알림 없음", state.reminderSummary)
  }
}

private suspend fun MainScreenViewModel.successState(
  predicate: (MealSignalScreenState) -> Boolean = { true },
): MealSignalScreenState =
  (uiState.first { it is MainScreenUiState.Success && predicate(it.state) } as MainScreenUiState.Success).state

private class FakeMealRepository(
  dashboard: MealSignalDashboard = sampleDashboard,
) : DataRepository {
  override val dashboard: Flow<MealSignalDashboard> = flowOf(dashboard)
}
