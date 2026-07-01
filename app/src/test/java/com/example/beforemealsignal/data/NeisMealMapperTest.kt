package com.example.beforemealsignal.data

import java.util.Calendar
import java.util.GregorianCalendar
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NeisMealMapperTest {
  @Test
  fun parseMenuItems_splitsBreaksAndMapsKoreanAllergenLabels() {
    val items = NeisMealMapper.parseMenuItems("잡곡밥<br/>계란찜 (1)<br/>새우튀김 (6.9)<br/>돈육볶음 (5, 10)")

    assertEquals(listOf("잡곡밥", "계란찜", "새우튀김", "돈육볶음"), items.map { it.name })
    assertEquals(setOf("계란"), items[1].allergens)
    assertEquals(setOf("밀", "새우"), items[2].allergens)
    assertEquals(setOf("대두", "돼지고기"), items[3].allergens)
  }

  @Test
  fun parseMealRows_readsNormalNeisEnvelope() {
    val body =
      """
      {
        "mealServiceDietInfo": [
          { "head": [{ "list_total_count": 1 }] },
          {
            "row": [
              {
                "SCHUL_NM": "선린인터넷고등학교",
                "MMEAL_SC_NM": "중식",
                "MLSV_YMD": "20260701",
                "DDISH_NM": "잡곡밥<br/>계란찜 (1)"
              }
            ]
          }
        ]
      }
      """
        .trimIndent()

    val rows = HttpNeisApiClient().parseMealRows(body)

    assertEquals(1, rows.size)
    assertEquals("20260701", rows.single().servedDate)
    assertEquals("잡곡밥<br/>계란찜 (1)", rows.single().dishName)
  }

  @Test
  fun weeklyMealParams_omitsMealCodeByDefaultAndKeepsExplicitOverride() {
    val client = HttpNeisApiClient()
    val weekRange = fixedWeekRange()

    val defaultParams = client.weeklyMealParams(NeisConfig("key", "B10", "7010536", ""), weekRange)
    val lunchParams = client.weeklyMealParams(NeisConfig("key", "B10", "7010536", "2"), weekRange)

    assertFalse(defaultParams.containsKey("MMEAL_SC_CODE"))
    assertEquals("2", lunchParams["MMEAL_SC_CODE"])
  }

  @Test
  fun dashboard_groupsRowsIntoBreakfastLunchDinnerSections() {
    val dashboard =
      NeisMealMapper.dashboard(
        rows =
          listOf(
            NeisMealRow("선린인터넷고등학교", "중식", "20260701", "잡곡밥<br/>계란찜 (1)"),
            NeisMealRow("선린인터넷고등학교", "조식", "20260701", "시리얼<br/>우유 (2)"),
            NeisMealRow("선린인터넷고등학교", "석식", "20260701", "김치볶음밥<br/>바나나"),
          ),
        weekRange = fixedWeekRange(),
        sourceLabel = "NEIS 실시간 연동",
      )

    val today = dashboard.weekMeals.first { it.isToday }

    assertEquals(listOf("아침", "점심", "저녁"), today.mealSections.map { it.displayName })
    assertEquals(listOf("시리얼", "우유"), today.mealSections[0].menuItems.map { it.name })
    assertEquals(listOf("잡곡밥", "계란찜"), today.mealSections[1].menuItems.map { it.name })
    assertEquals(listOf("김치볶음밥", "바나나"), today.mealSections[2].menuItems.map { it.name })
    assertEquals(listOf("시리얼", "우유", "잡곡밥", "계란찜", "김치볶음밥", "바나나"), today.menuItems.map { it.name })
  }

  @Test
  fun fallbackDashboard_keepsWeekMealsAndReportTargetsNonEmpty() {
    val weekRange = fixedWeekRange()

    val dashboard = NeisMealMapper.fallbackDashboard(weekRange, "NEIS 데이터 없음")

    assertEquals("NEIS 데이터 없음", dashboard.sourceLabel)
    assertEquals("6월 29일 - 7월 3일", dashboard.weekLabel)
    assertEquals(5, dashboard.weekMeals.size)
    assertTrue(dashboard.weekMeals.all { it.menuItems.isEmpty() })
    assertTrue(dashboard.weekMeals.all { it.mealSections.map { section -> section.displayName } == listOf("아침", "점심", "저녁") })
    assertTrue(dashboard.reportTargets.isNotEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun repository_missingConfigReturnsFallbackWithoutCallingClient() = runTest {
    val client = RecordingNeisApiClient(emptyList())
    val repository =
      NeisMealRepository(
        config = NeisConfig(apiKey = "", officeCode = "", schoolCode = "", mealCode = "2"),
        fetchWeeklyMeals = client::fetchWeeklyMeals,
        todayProvider = { fixedDate() },
        ioDispatcher = UnconfinedTestDispatcher(testScheduler),
      )

    val dashboard = repository.dashboard.first()

    assertEquals("NEIS 설정 필요", dashboard.sourceLabel)
    assertFalse(client.wasCalled)
    assertTrue(dashboard.weekMeals.isNotEmpty())
    assertTrue(dashboard.reportTargets.isNotEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun repository_emptyRowsReturnsNoDataFallback() = runTest {
    val repository =
      NeisMealRepository(
        config = NeisConfig(apiKey = "key", officeCode = "B10", schoolCode = "7010536", mealCode = "2"),
        fetchWeeklyMeals = RecordingNeisApiClient(emptyList())::fetchWeeklyMeals,
        todayProvider = { fixedDate() },
        ioDispatcher = UnconfinedTestDispatcher(testScheduler),
      )

    val dashboard = repository.dashboard.first()

    assertEquals("NEIS 데이터 없음", dashboard.sourceLabel)
    assertTrue(dashboard.weekMeals.isNotEmpty())
    assertTrue(dashboard.reportTargets.isNotEmpty())
  }

  private fun fixedWeekRange(): NeisWeekRange = NeisWeekRanges.currentSchoolWeek(fixedDate())

  private fun fixedDate() = GregorianCalendar(2026, Calendar.JULY, 1).time
}

private class RecordingNeisApiClient(private val rows: List<NeisMealRow>) {
  var wasCalled: Boolean = false
    private set

  suspend fun fetchWeeklyMeals(config: NeisConfig, weekRange: NeisWeekRange): List<NeisMealRow> {
    wasCalled = true
    return rows
  }
}
