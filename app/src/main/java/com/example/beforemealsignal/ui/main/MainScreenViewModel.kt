package com.example.beforemealsignal.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beforemealsignal.data.DataRepository
import com.example.beforemealsignal.data.MealDay
import com.example.beforemealsignal.data.MealItem
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
    prototypeState.update { it.copy(notificationsEnabled = !it.notificationsEnabled) }
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
  val todayMeal: MealDay = dashboard.weekMeals.firstOrNull { it.isToday } ?: dashboard.weekMeals.first()
  val selectedMeal: MealDay = dashboard.weekMeals.getOrElse(local.selectedDayIndex) { todayMeal }
  val todayMatchedAllergens: List<String> = todayMeal.matchedAllergens(selectedAllergens)
  val todayEstimated: Boolean = todayMeal.menuItems.any { it.isEstimated }
  val todaySpicyLevel: Int = todayMeal.menuItems.maxOfOrNull { it.spicyLevel } ?: 0
  val riskMenuName: String? = todayMeal.menuItems.firstOrNull { item -> item.allergens.any { it in selectedAllergens } }?.name
}

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
  val notificationsEnabled: Boolean = true,
)

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
