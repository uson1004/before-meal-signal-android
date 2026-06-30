package com.example.beforemealsignal.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface DataRepository {
  val dashboard: Flow<FoodSignalDashboard>
}

class DefaultDataRepository : DataRepository {
  override val dashboard: Flow<FoodSignalDashboard> = flowOf(sampleDashboard)
}

data class FoodSignalDashboard(
  val profile: DietProfile,
  val quickActions: List<QuickAction>,
  val foods: List<FoodSignal>,
  val staffPrompt: String,
)

data class DietProfile(
  val allergens: List<String>,
  val dietTags: List<String>,
  val spicyTolerance: Int,
)

data class QuickAction(
  val title: String,
  val description: String,
  val state: SignalTone,
)

data class FoodSignal(
  val name: String,
  val brand: String,
  val category: String,
  val source: String,
  val updatedAt: String,
  val confidence: String,
  val headline: String,
  val summary: String,
  val riskLevel: RiskLevel,
  val matchedAllergens: List<String>,
  val possibleAllergens: List<String>,
  val spicyLevel: Int,
  val cautionTags: List<String>,
  val recommendedAction: String,
)

enum class RiskLevel {
  High,
  Check,
  Low,
}

enum class SignalTone {
  Danger,
  Warning,
  Safe,
  Info,
}

private val sampleDashboard =
  FoodSignalDashboard(
    profile =
      DietProfile(
        allergens = listOf("우유", "대두", "새우"),
        dietTags = listOf("매운맛 약함", "카페인 민감", "주문 전 확인"),
        spicyTolerance = 1,
      ),
    quickActions =
      listOf(
        QuickAction("제품/메뉴 검색", "제품명, 메뉴명, 브랜드로 확인", SignalTone.Info),
        QuickAction("바코드·식품QR", "공식 표시 데이터를 우선 조회", SignalTone.Safe),
        QuickAction("성분표 촬영", "OCR 실험 기능, 결과는 확인 필요", SignalTone.Warning),
      ),
    foods =
      listOf(
        FoodSignal(
          name = "참치마요 삼각김밥",
          brand = "편의점 PB",
          category = "가공식품",
          source = "식품QR/제조사 제공 성분",
          updatedAt = "2026.06.24",
          confidence = "공식 데이터",
          headline = "섭취 전 확인 필요",
          summary = "등록한 우유·대두 알레르기와 충돌하는 원재료가 표시되어 있습니다.",
          riskLevel = RiskLevel.High,
          matchedAllergens = listOf("우유", "대두"),
          possibleAllergens = listOf("밀"),
          spicyLevel = 0,
          cautionTags = listOf("공식 정보 기준", "교차오염 가능성 확인"),
          recommendedAction = "대체 제품을 찾거나 제조사 표시를 다시 확인하세요.",
        ),
        FoodSignal(
          name = "마라탕 2단계",
          brand = "동네 중식당",
          category = "음식점 메뉴",
          source = "메뉴명/사용자 평가 기반 추정",
          updatedAt = "2026.06.18",
          confidence = "추정 정보",
          headline = "정보 부족",
          summary = "공식 성분표가 없어 새우·대두 포함 가능성을 낮은 신뢰도로 표시합니다.",
          riskLevel = RiskLevel.Check,
          matchedAllergens = emptyList(),
          possibleAllergens = listOf("새우", "대두"),
          spicyLevel = 4,
          cautionTags = listOf("매운맛 약한 사용자 주의", "직원 확인 권장"),
          recommendedAction = "주문 전 육수와 소스 원재료를 직원에게 확인하세요.",
        ),
        FoodSignal(
          name = "오트 라떼",
          brand = "프랜차이즈 카페",
          category = "프랜차이즈",
          source = "브랜드 알레르겐 안내",
          updatedAt = "2026.06.20",
          confidence = "브랜드 제공",
          headline = "확인 필요",
          summary = "우유 대체 옵션이지만 같은 제조 공간의 우유 접촉 가능성이 표시되어 있습니다.",
          riskLevel = RiskLevel.Check,
          matchedAllergens = emptyList(),
          possibleAllergens = listOf("우유"),
          spicyLevel = 0,
          cautionTags = listOf("카페인 확인", "교차오염 가능성"),
          recommendedAction = "카페인 제한과 제조 도구 분리 여부를 확인하세요.",
        ),
        FoodSignal(
          name = "두부 샐러드 볼",
          brand = "샐러드 프랜차이즈",
          category = "프랜차이즈",
          source = "브랜드 공식 메뉴 데이터",
          updatedAt = "2026.06.22",
          confidence = "공식 데이터",
          headline = "주의 낮음",
          summary = "등록 알레르겐과 직접 충돌하는 표시 정보는 없습니다. 소스 원재료는 주문 전 확인하세요.",
          riskLevel = RiskLevel.Low,
          matchedAllergens = emptyList(),
          possibleAllergens = listOf("대두"),
          spicyLevel = 1,
          cautionTags = listOf("정보 없음은 안전 보장 아님", "소스 별도 확인"),
          recommendedAction = "소스 제외 또는 대체 소스 선택이 안전합니다.",
        ),
      ),
    staffPrompt = "우유, 대두, 새우 알레르기가 있습니다. 이 메뉴에 해당 성분이나 같은 조리 도구 접촉 가능성이 있나요?",
  )
