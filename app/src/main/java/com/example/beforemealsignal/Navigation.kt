package com.example.beforemealsignal

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.beforemealsignal.data.DataRepository
import com.example.beforemealsignal.ui.main.MainScreen

@Composable
fun MainNavigation(dataRepository: DataRepository) {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(
            onItemClick = { navKey -> backStack.add(navKey) },
            dataRepository = dataRepository,
            modifier = Modifier.safeDrawingPadding(),
          )
        }
      },
  )
}
