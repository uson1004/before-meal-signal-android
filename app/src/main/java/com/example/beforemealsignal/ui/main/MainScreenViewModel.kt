package com.example.beforemealsignal.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beforemealsignal.data.DataRepository
import com.example.beforemealsignal.data.FoodSignal
import com.example.beforemealsignal.data.FoodSignalDashboard
import com.example.beforemealsignal.ui.main.MainScreenUiState.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MainScreenViewModel(dataRepository: DataRepository) : ViewModel() {
  private val query = MutableStateFlow("")
  private val selectedFoodName = MutableStateFlow<String?>(null)

  val uiState: StateFlow<MainScreenUiState> =
    combine(dataRepository.dashboard, query, selectedFoodName) { dashboard, searchQuery, selectedName ->
        val filteredFoods = dashboard.foods.filterBy(searchQuery)
        val selectedFood = filteredFoods.firstOrNull { it.name == selectedName } ?: filteredFoods.firstOrNull()
        FoodSignalScreenState(
          dashboard = dashboard,
          query = searchQuery,
          filteredFoods = filteredFoods,
          selectedFood = selectedFood,
        )
      }
      .map<FoodSignalScreenState, MainScreenUiState>(::Success)
      .catch { emit(MainScreenUiState.Error(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Loading)

  fun onQueryChange(value: String) {
    query.value = value
  }

  fun onFoodSelected(food: FoodSignal) {
    selectedFoodName.value = food.name
  }
}

sealed interface MainScreenUiState {
  object Loading : MainScreenUiState

  data class Error(val throwable: Throwable) : MainScreenUiState

  data class Success(val state: FoodSignalScreenState) : MainScreenUiState
}

data class FoodSignalScreenState(
  val dashboard: FoodSignalDashboard,
  val query: String,
  val filteredFoods: List<FoodSignal>,
  val selectedFood: FoodSignal?,
)

private fun List<FoodSignal>.filterBy(query: String): List<FoodSignal> {
  val normalized = query.trim()
  if (normalized.isEmpty()) return this
  return filter { food ->
    food.name.contains(normalized, ignoreCase = true) ||
      food.brand.contains(normalized, ignoreCase = true) ||
      food.category.contains(normalized, ignoreCase = true) ||
      food.matchedAllergens.any { it.contains(normalized, ignoreCase = true) } ||
      food.possibleAllergens.any { it.contains(normalized, ignoreCase = true) }
  }
}
