package com.example.beforemealsignal.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beforemealsignal.data.DataRepository
import com.example.beforemealsignal.data.MealDay
import com.example.beforemealsignal.data.MealItem
import com.example.beforemealsignal.data.MealSection
import com.example.beforemealsignal.data.MealSignalDashboard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MainScreenViewModel(dataRepository: DataRepository) : ViewModel() {
  private val prototypeState = MutableStateFlow(MealPrototypeState())

  val uiState: StateFlow<MainScreenUiState> =
    combine(dataRepository.dashboard, prototypeState) { dashboard, localState ->
        MealSignalScreenState(dashboard = dashboard, local = localState)
      }
      .map<MealSignalScreenState, MainScreenUiState>(MainScreenUiState::Success)
      .catch { emit(MainScreenUiState.Error(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Loading)

  fun onAllergenToggled(allergen: String) {
    prototypeState.update { state ->
      state.copy(
        selectedAllergens =
          if (allergen in state.selectedAllergens) state.selectedAllergens - allergen
          else state.selectedAllergens + allergen,
      )
    }
  }

  fun onSpicyToleranceSelected(level: Int) {
    prototypeState.update { it.copy(spicyTolerance = level.coerceIn(0, 2)) }
  }

  fun onStartOnboarding() {
    prototypeState.update { it.copy(showOnboarding = false, activeTab = MealTab.Home) }
  }

  fun onSkipOnboarding() {
    prototypeState.update { it.copy(showOnboarding = false, activeTab = MealTab.Week) }
  }

  fun onTabSelected(tab: MealTab) {
    prototypeState.update { it.copy(activeTab = tab, reportSubmitted = false) }
  }

  fun onDaySelected(index: Int) {
    prototypeState.update { it.copy(selectedDayIndex = index) }
  }

  fun onReportSpicySelected(level: Int) {
    prototypeState.update { it.copy(reportSpicyRating = level.coerceIn(0, 4), reportSubmitted = false) }
  }

  fun onReportAllergenToggled(allergen: String) {
    prototypeState.update { state ->
      val next =
        if (allergen == "없음") {
          setOf("없음")
        } else {
          val withoutNone = state.reportAllergens - "없음"
          if (allergen in withoutNone) withoutNone - allergen else withoutNone + allergen
        }
      state.copy(reportAllergens = next, reportSubmitted = false)
    }
  }

  fun onPhotoToggle() {
    prototypeState.update { it.copy(photoAttached = !it.photoAttached, reportSubmitted = false) }
  }

  fun onSubmitReport() {
    prototypeState.update { it.copy(reportSubmitted = true, submittedReports = it.submittedReports + 1) }
  }

  fun onEditProfile() {
    prototypeState.update { it.copy(showOnboarding = true) }
  }

  fun onNotificationsToggle() {
    prototypeState.update { state ->
      state.copy(reminderSettings = state.reminderSettings.copy(enabled = !state.reminderSettings.enabled))
    }
  }

  fun onReminderMealToggled(mealPeriod: String) {
    if (mealPeriod !in reminderMealPeriodOptions) return

    prototypeState.update { state ->
      val current = state.reminderSettings.mealPeriods
      val next =
        if (mealPeriod in current) {
          current.minus(mealPeriod).ifEmpty { current }
        } else {
          current + mealPeriod
        }
      state.copy(reminderSettings = state.reminderSettings.copy(mealPeriods = next))
    }
  }

  fun onReminderLeadSelected(minutes: Int) {
    if (minutes !in reminderLeadOptions) return

    prototypeState.update { state ->
      state.copy(reminderSettings = state.reminderSettings.copy(leadMinutes = minutes))
    }
  }
}

sealed interface MainScreenUiState {
  data object Loading : MainScreenUiState

  data class Error(val throwable: Throwable) : MainScreenUiState

  data class Success(val state: MealSignalScreenState) : MainScreenUiState
}

data class MealSignalScreenState(
  val dashboard: MealSignalDashboard,
  val local: MealPrototypeState,
) {
  val selectedAllergens: Set<String> = local.selectedAllergens
  val spicyTolerance: Int = local.spicyTolerance ?: dashboard.profile.spicyTolerance
  val streakDays: Int = dashboard.profile.streakDays + local.submittedReports
  val reportCount: Int = dashboard.profile.reportCount + local.submittedReports
  val todayMeal: MealDay = dashboard.weekMeals.firstOrNull { it.isToday } ?: dashboard.weekMeals.firstOrNull() ?: emptyMealDay
  val selectedMeal: MealDay = dashboard.weekMeals.getOrElse(local.selectedDayIndex) { todayMeal }
  val todayMatchedAllergens: List<String> = todayMeal.matchedAllergens(selectedAllergens)
  val todayEstimated: Boolean = todayMeal.menuItems.any { it.isEstimated }
  val todaySpicyLevel: Int = todayMeal.menuItems.maxOfOrNull { it.spicyLevel } ?: 0
  val riskMenuName: String? = todayMeal.menuItems.firstOrNull { item -> item.allergens.any { it in selectedAllergens } }?.name
  val activeReminderPeriods: List<String> =
    if (local.reminderSettings.enabled) {
      val servedPeriods = todayMeal.mealSections.filter { it.menuItems.isNotEmpty() }.map { it.displayName }.toSet()
      reminderMealPeriodOptions.filter { it in local.reminderSettings.mealPeriods && it in servedPeriods }
    } else {
      emptyList()
    }
  val reminderSummary: String =
    when {
      !local.reminderSettings.enabled -> "알림 꺼짐"
      activeReminderPeriods.isEmpty() -> "오늘 알림 없음"
      else -> "${activeReminderPeriods.joinToString("·")} ${local.reminderSettings.leadMinutes}분 전"
    }
}

data class MealReminderSettings(
  val enabled: Boolean = true,
  val mealPeriods: Set<String> = setOf("아침", "점심", "저녁"),
  val leadMinutes: Int = 10,
)

data class MealPrototypeState(
  val showOnboarding: Boolean = true,
  val activeTab: MealTab = MealTab.Home,
  val selectedAllergens: Set<String> = setOf("계란", "우유"),
  val spicyTolerance: Int? = 1,
  val selectedDayIndex: Int = 1,
  val reportSpicyRating: Int = 2,
  val reportAllergens: Set<String> = setOf("계란"),
  val photoAttached: Boolean = false,
  val reportSubmitted: Boolean = false,
  val submittedReports: Int = 0,
  val reminderSettings: MealReminderSettings = MealReminderSettings(),
) {
  val notificationsEnabled: Boolean
    get() = reminderSettings.enabled
}

enum class MealTab(val label: String) {
  Home("홈"),
  Week("주간"),
  Report("제보"),
  Profile("프로필"),
}

fun MealDay.matchedAllergens(selectedAllergens: Set<String>): List<String> =
  menuItems.flatMap { it.allergens }.filter { it in selectedAllergens }.distinct()

fun MealItem.statusLabel(selectedAllergens: Set<String>): String =
  when {
    allergens.any { it in selectedAllergens } -> allergens.first { it in selectedAllergens }
    isEstimated -> "추정"
    spicyLevel >= 2 -> "매움"
    spicyLevel == 1 -> "약간매움"
    else -> "표시없음"
  }

val reminderMealPeriodOptions: List<String> = listOf("아침", "점심", "저녁")

val reminderLeadOptions: List<Int> = listOf(10, 20, 30)

private val emptyMealDay =
  MealDay(
    dateLabel = "오늘",
    fullDateLabel = "오늘",
    dayBadge = "-",
    isToday = true,
    mealSections =
      listOf(
        MealSection(displayName = "아침", mealType = "조식", menuItems = emptyList()),
        MealSection(displayName = "점심", mealType = "중식", menuItems = emptyList()),
        MealSection(displayName = "저녁", mealType = "석식", menuItems = emptyList()),
      ),
  )
