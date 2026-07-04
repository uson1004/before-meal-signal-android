package com.example.beforemealsignal.data

import java.util.Date
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class NeisMealRepository(
  private val config: NeisConfig,
  private val fetchWeeklyMeals: suspend (NeisConfig, NeisWeekRange) -> List<NeisMealRow> = HttpNeisApiClient()::fetchWeeklyMeals,
  private val todayProvider: () -> Date = { Date() },
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DataRepository {
  override val dashboard: Flow<MealSignalDashboard> =
    flow {
        val weekRange = NeisWeekRanges.currentSchoolWeek(todayProvider())
        if (!config.isConfigured) {
          emit(NeisMealMapper.fallbackDashboard(weekRange, "NEIS 설정 필요"))
          return@flow
        }

        val rows = fetchWeeklyMeals(config, weekRange)
        emit(
          if (rows.isEmpty()) {
            NeisMealMapper.fallbackDashboard(weekRange, "NEIS 데이터 없음")
          } else {
            NeisMealMapper.dashboard(rows = rows, weekRange = weekRange, sourceLabel = "NEIS 실시간 연동")
          },
        )
      }
      .catch { emit(NeisMealMapper.fallbackDashboard(NeisWeekRanges.currentSchoolWeek(todayProvider()), "NEIS 연결 실패")) }
      .flowOn(ioDispatcher)
}

object NeisMealMapper {
  val allergenLabelsByCode: Map<Int, String> =
    mapOf(
      1 to "계란",
      2 to "우유",
      3 to "메밀",
      4 to "땅콩",
      5 to "대두",
      6 to "밀",
      7 to "고등어",
      8 to "게",
      9 to "새우",
      10 to "돼지고기",
      11 to "복숭아",
      12 to "토마토",
      13 to "아황산류",
      14 to "호두",
      15 to "닭고기",
      16 to "소고기",
      17 to "오징어",
      18 to "조개류",
      19 to "잣",
    )

  fun dashboard(rows: List<NeisMealRow>, weekRange: NeisWeekRange, sourceLabel: String): MealSignalDashboard {
    val rowsByDate = rows.groupBy { it.servedDate }
    val weekMeals =
      weekRange.days.map { day ->
        val dayRows = rowsByDate[day.ymd].orEmpty()
        MealDay(
          dateLabel = day.dateLabel,
          fullDateLabel = day.fullDateLabel,
          dayBadge = day.dayBadge,
          isToday = day.isToday,
          mealSections = mealSections(dayRows),
        )
      }

    return baseDashboard(weekRange = weekRange, sourceLabel = sourceLabel, weekMeals = weekMeals, reportTargets = reportTargets(weekMeals))
  }

  fun fallbackDashboard(weekRange: NeisWeekRange, sourceLabel: String): MealSignalDashboard {
    val weekMeals =
      weekRange.days.map { day ->
        MealDay(
          dateLabel = day.dateLabel,
          fullDateLabel = day.fullDateLabel,
          dayBadge = day.dayBadge,
          isToday = day.isToday,
          mealSections = emptyMealSections(),
        )
      }
    return baseDashboard(
      weekRange = weekRange,
      sourceLabel = sourceLabel,
      weekMeals = weekMeals,
      reportTargets = listOf(ReportTarget("급식 정보 없음", "NEIS 급식 데이터를 불러오지 못했어요.")),
    )
  }

  fun parseMenuItems(dishName: String): List<MealItem> =
    dishName
      .split(Regex("(?i)<br\\s*/?>"))
      .map { it.cleanHtml() }
      .filter { it.isNotBlank() }
      .map { rawItem ->
        val allergenCodes = allergyGroupRegex.findAll(rawItem).flatMap { codeRegex.findAll(it.value).map { match -> match.value.toIntOrNull() } }.filterNotNull()
        val allergens = allergenCodes.mapNotNull(allergenLabelsByCode::get).toSet()
        val displayName =
          allergyGroupRegex
            .replace(rawItem, "")
            .replace("(*)", "")
            .replace(Regex("\\s+"), " ")
            .trim()
        val spicyLevel = estimateSpicyLevel(displayName)
        MealItem(name = displayName.ifBlank { rawItem }, allergens = allergens, spicyLevel = spicyLevel, isEstimated = spicyLevel > 0)
      }

  private fun baseDashboard(
    weekRange: NeisWeekRange,
    sourceLabel: String,
    weekMeals: List<MealDay>,
    reportTargets: List<ReportTarget>,
  ): MealSignalDashboard =
    MealSignalDashboard(
      profile = sampleDashboard.profile,
      weekLabel = weekRange.weekLabel(),
      sourceLabel = sourceLabel,
      allergenOptions = sampleDashboard.allergenOptions,
      weekMeals = weekMeals.ifEmpty { fallbackDashboard(weekRange, sourceLabel).weekMeals },
      reportTargets = reportTargets.ifEmpty { listOf(ReportTarget("급식 정보 없음", "NEIS 급식 데이터를 불러오지 못했어요.")) },
    )

  private fun reportTargets(weekMeals: List<MealDay>): List<ReportTarget> =
    weekMeals
      .firstOrNull { it.isToday }
      ?.let { today ->
        today.mealSections.firstOrNull { it.displayName == "점심" && it.menuItems.isNotEmpty() }?.menuItems ?: today.menuItems
      }
      .orEmpty()
      .take(3)
      .map { ReportTarget(it.name, "오늘 급식에서 체감 매운맛이나 알레르기 표시를 알려주세요.") }
      .ifEmpty { listOf(ReportTarget("급식 정보 없음", "NEIS 급식 데이터를 불러오지 못했어요.")) }

  private fun mealSections(dayRows: List<NeisMealRow>): List<MealSection> {
    val rowsByMeal = dayRows.groupBy { canonicalMealName(it.mealName) }
    val knownSections =
      mealOrder.map { mealName ->
        MealSection(
          displayName = mealDisplayName(mealName),
          mealType = mealName,
          menuItems = rowsByMeal[mealName].orEmpty().flatMap { parseMenuItems(it.dishName) },
        )
      }
    val extraSections =
      rowsByMeal
        .keys
        .filterNot { it in mealOrder }
        .sorted()
        .map { mealName ->
          MealSection(
            displayName = mealDisplayName(mealName),
            mealType = mealName,
            menuItems = rowsByMeal[mealName].orEmpty().flatMap { parseMenuItems(it.dishName) },
          )
        }
    return knownSections + extraSections
  }

  private fun canonicalMealName(mealName: String): String =
    when {
      "조식" in mealName -> "조식"
      "중식" in mealName -> "중식"
      "석식" in mealName -> "석식"
      else -> mealName.trim().ifBlank { "중식" }
    }

  private fun emptyMealSections(): List<MealSection> =
    mealOrder.map { mealName -> MealSection(displayName = mealDisplayName(mealName), mealType = mealName, menuItems = emptyList()) }

  private fun mealDisplayName(mealName: String): String =
    when {
      "조식" in mealName -> "아침"
      "중식" in mealName -> "점심"
      "석식" in mealName -> "저녁"
      else -> mealName.ifBlank { "기타" }
    }

  private fun NeisWeekRange.weekLabel(): String {
    val start = days.first()
    val end = days.last()
    return "${start.fullDateLabel.substringBeforeLast(" ")} - ${end.fullDateLabel.substringBeforeLast(" ")}"
  }

  private fun String.cleanHtml(): String =
    replace("&amp;", "&")
      .replace("&lt;", "<")
      .replace("&gt;", ">")
      .replace("&nbsp;", " ")
      .replace(Regex("<[^>]+>"), "")
      .trim()

  private fun estimateSpicyLevel(name: String): Int =
    when {
      listOf("마라", "불닭", "매운", "불고추").any { it in name } -> 2
      listOf("김치", "깍두기", "제육", "떡볶", "고추장", "짬뽕", "칠리").any { it in name } -> 1
      else -> 0
    }

  private val allergyGroupRegex = Regex("\\([^)]*\\d[^)]*\\)")
  private val codeRegex = Regex("\\d+")
  private val mealOrder = listOf("조식", "중식", "석식")
}
