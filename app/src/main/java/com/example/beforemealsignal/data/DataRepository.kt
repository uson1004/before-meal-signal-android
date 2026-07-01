package com.example.beforemealsignal.data

import kotlinx.coroutines.flow.Flow

interface DataRepository {
  val dashboard: Flow<MealSignalDashboard>
}

data class MealSignalDashboard(
  val profile: StudentProfile,
  val weekLabel: String,
  val sourceLabel: String,
  val allergenOptions: List<String>,
  val weekMeals: List<MealDay>,
  val reportTargets: List<ReportTarget>,
)

data class StudentProfile(
  val name: String,
  val classLabel: String,
  val allergens: Set<String>,
  val spicyTolerance: Int,
  val streakDays: Int,
  val reportCount: Int,
)

data class MealDay(
  val dateLabel: String,
  val fullDateLabel: String,
  val dayBadge: String,
  val isToday: Boolean,
  val mealSections: List<MealSection>,
) {
  val menuItems: List<MealItem>
    get() = mealSections.flatMap { it.menuItems }

  val mealType: String
    get() =
      mealSections
        .filter { it.menuItems.isNotEmpty() }
        .joinToString("·") { it.displayName }
        .ifBlank { "급식" }
}

data class MealSection(
  val displayName: String,
  val mealType: String,
  val menuItems: List<MealItem>,
)

data class MealItem(
  val name: String,
  val allergens: Set<String> = emptySet(),
  val spicyLevel: Int = 0,
  val isEstimated: Boolean = false,
)

data class ReportTarget(
  val menuName: String,
  val description: String,
)

private fun mealSections(
  breakfast: List<MealItem> = emptyList(),
  lunch: List<MealItem> = emptyList(),
  dinner: List<MealItem> = emptyList(),
): List<MealSection> =
  listOf(
    MealSection(displayName = "아침", mealType = "조식", menuItems = breakfast),
    MealSection(displayName = "점심", mealType = "중식", menuItems = lunch),
    MealSection(displayName = "저녁", mealType = "석식", menuItems = dinner),
  )

val sampleDashboard =
  MealSignalDashboard(
    profile =
      StudentProfile(
        name = "김민준",
        classLabel = "2학년 3반",
        allergens = setOf("계란", "우유"),
        spicyTolerance = 1,
        streakDays = 7,
        reportCount = 12,
      ),
    weekLabel = "6월 29일 - 7월 3일",
    sourceLabel = "NEIS 연동 샘플",
    allergenOptions = listOf("계란", "우유", "새우", "땅콩", "밀", "메밀", "대두", "돼지고기"),
    weekMeals =
      listOf(
        MealDay(
          dateLabel = "월 6/29",
          fullDateLabel = "6월 29일 월요일",
          dayBadge = "✓",
          isToday = false,
          mealSections =
            mealSections(
              lunch =
                listOf(
                  MealItem("현미밥"),
                  MealItem("미역국"),
                  MealItem("돈까스", allergens = setOf("밀", "돼지고기")),
                  MealItem("양배추샐러드"),
                ),
            ),
        ),
        MealDay(
          dateLabel = "오늘 6/30",
          fullDateLabel = "6월 30일 화요일",
          dayBadge = "화",
          isToday = true,
          mealSections =
            mealSections(
              lunch =
                listOf(
                  MealItem("잡곡밥"),
                  MealItem("된장찌개", allergens = setOf("대두"), spicyLevel = 1),
                  MealItem("제육볶음", allergens = setOf("돼지고기"), spicyLevel = 2, isEstimated = true),
                  MealItem("계란찜", allergens = setOf("계란")),
                  MealItem("배추김치", spicyLevel = 1),
                ),
            ),
        ),
        MealDay(
          dateLabel = "수 7/1",
          fullDateLabel = "7월 1일 수요일",
          dayBadge = "3",
          isToday = false,
          mealSections =
            mealSections(
              lunch =
                listOf(
                  MealItem("흰밥"),
                  MealItem("어묵국", allergens = setOf("밀")),
                  MealItem("새우튀김", allergens = setOf("새우", "밀")),
                  MealItem("깍두기", spicyLevel = 1),
                ),
            ),
        ),
        MealDay(
          dateLabel = "목 7/2",
          fullDateLabel = "7월 2일 목요일",
          dayBadge = "4",
          isToday = false,
          mealSections =
            mealSections(
              lunch =
                listOf(
                  MealItem("카레라이스", allergens = setOf("밀"), spicyLevel = 1),
                  MealItem("요구르트", allergens = setOf("우유")),
                  MealItem("오이무침"),
                ),
            ),
        ),
        MealDay(
          dateLabel = "금 7/3",
          fullDateLabel = "7월 3일 금요일",
          dayBadge = "5",
          isToday = false,
          mealSections =
            mealSections(
              lunch =
                listOf(
                  MealItem("김치볶음밥", spicyLevel = 2),
                  MealItem("콩나물국", allergens = setOf("대두")),
                  MealItem("바나나"),
                ),
            ),
        ),
      ),
    reportTargets =
      listOf(
        ReportTarget("제육볶음", "오늘 급식에서 체감 매운맛을 알려주세요."),
        ReportTarget("된장찌개", "국물의 매운맛이나 알레르기 표시를 보완해 주세요."),
        ReportTarget("계란찜", "계란 알레르기 표시가 맞는지 확인해 주세요."),
      ),
  )
