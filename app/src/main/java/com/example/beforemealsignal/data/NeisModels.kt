package com.example.beforemealsignal.data

import com.example.beforemealsignal.BuildConfig
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class NeisConfig(
  val apiKey: String,
  val officeCode: String,
  val schoolCode: String,
  val mealCode: String,
) {
  val isConfigured: Boolean
    get() = apiKey.isNotBlank() && officeCode.isNotBlank() && schoolCode.isNotBlank()

  companion object {
    fun fromBuildConfig(): NeisConfig =
      NeisConfig(
        apiKey = BuildConfig.NEIS_API_KEY,
        officeCode = BuildConfig.NEIS_OFFICE_CODE,
        schoolCode = BuildConfig.NEIS_SCHOOL_CODE,
        mealCode = BuildConfig.NEIS_MEAL_CODE,
      )
  }
}

data class NeisMealRow(
  val schoolName: String,
  val mealName: String,
  val servedDate: String,
  val dishName: String,
)

data class NeisWeekRange(
  val startYmd: String,
  val endYmd: String,
  val days: List<NeisWeekDay>,
)

data class NeisWeekDay(
  val ymd: String,
  val dateLabel: String,
  val fullDateLabel: String,
  val dayBadge: String,
  val isToday: Boolean,
)

object NeisWeekRanges {
  fun currentSchoolWeek(today: Date = Date()): NeisWeekRange {
    val todayCalendar = calendar(today)
    val monday = calendar(today)
    val dayOfWeek = monday.get(Calendar.DAY_OF_WEEK)
    val offsetToMonday = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
    monday.add(Calendar.DAY_OF_MONTH, offsetToMonday)

    val days =
      (0 until 5).map { offset ->
        val day = monday.clone() as Calendar
        day.add(Calendar.DAY_OF_MONTH, offset)
        val isToday = sameDay(day, todayCalendar)
        val month = day.get(Calendar.MONTH) + 1
        val date = day.get(Calendar.DAY_OF_MONTH)
        val dayName = koreanDayName(day.get(Calendar.DAY_OF_WEEK))
        NeisWeekDay(
          ymd = ymd(day),
          dateLabel = if (isToday) "오늘 $month/$date" else "$dayName $month/$date",
          fullDateLabel = "${month}월 ${date}일 ${koreanFullDayName(day.get(Calendar.DAY_OF_WEEK))}",
          dayBadge = if (isToday) dayName else date.toString(),
          isToday = isToday,
        )
      }

    return NeisWeekRange(
      startYmd = days.first().ymd,
      endYmd = days.last().ymd,
      days = days,
    )
  }

  private fun calendar(date: Date): Calendar =
    Calendar.getInstance(Locale.KOREA).apply {
      firstDayOfWeek = Calendar.MONDAY
      time = date
    }

  private fun sameDay(left: Calendar, right: Calendar): Boolean =
    left.get(Calendar.YEAR) == right.get(Calendar.YEAR) &&
      left.get(Calendar.DAY_OF_YEAR) == right.get(Calendar.DAY_OF_YEAR)

  private fun ymd(calendar: Calendar): String =
    "%04d%02d%02d".format(
      Locale.US,
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH) + 1,
      calendar.get(Calendar.DAY_OF_MONTH),
    )

  private fun koreanDayName(dayOfWeek: Int): String =
    when (dayOfWeek) {
      Calendar.MONDAY -> "월"
      Calendar.TUESDAY -> "화"
      Calendar.WEDNESDAY -> "수"
      Calendar.THURSDAY -> "목"
      Calendar.FRIDAY -> "금"
      Calendar.SATURDAY -> "토"
      else -> "일"
    }

  private fun koreanFullDayName(dayOfWeek: Int): String = "${koreanDayName(dayOfWeek)}요일"
}
