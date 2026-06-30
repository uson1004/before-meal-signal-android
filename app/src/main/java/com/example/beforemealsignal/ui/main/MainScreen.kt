package com.example.beforemealsignal.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.beforemealsignal.data.DefaultDataRepository
import com.example.beforemealsignal.data.DietProfile
import com.example.beforemealsignal.data.FoodSignal
import com.example.beforemealsignal.data.FoodSignalDashboard
import com.example.beforemealsignal.data.QuickAction
import com.example.beforemealsignal.data.RiskLevel
import com.example.beforemealsignal.data.SignalTone
import com.example.beforemealsignal.theme.BeforeMealSignalTheme

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(DefaultDataRepository()) },
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  when (val current = state) {
    MainScreenUiState.Loading -> LoadingScreen(modifier)
    is MainScreenUiState.Success ->
      MainScreen(
        state = current.state,
        onQueryChange = viewModel::onQueryChange,
        onFoodSelected = viewModel::onFoodSelected,
        modifier = modifier,
      )
    is MainScreenUiState.Error -> ErrorScreen(current.throwable, modifier)
  }
}

@Composable
internal fun MainScreen(
  state: FoodSignalScreenState,
  onQueryChange: (String) -> Unit,
  onFoodSelected: (FoodSignal) -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    containerColor = SignalColors.Background,
    bottomBar = { SignalBottomBar() },
    modifier = modifier.fillMaxSize(),
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item { ProductHeader(state.dashboard.profile) }
      item { SearchAndActionPanel(state.dashboard.quickActions, state.query, onQueryChange) }
      item { ProfileSummaryCard(state.dashboard.profile) }
      item { SectionTitle("최근 확인한 식품·메뉴", "공식 데이터와 추정 정보를 구분해서 표시합니다.") }
      if (state.filteredFoods.isEmpty()) {
        item { EmptyResultCard(state.query) }
      } else {
        items(state.filteredFoods, key = { it.name }) { food ->
          FoodResultCard(
            food = food,
            selected = food.name == state.selectedFood?.name,
            onClick = { onFoodSelected(food) },
          )
        }
      }
      state.selectedFood?.let { selected ->
        item { RiskDetailCard(selected) }
        item { StaffPromptCard(state.dashboard.staffPrompt) }
      }
      item { SafetyPolicyCard() }
    }
  }
}

@Composable
private fun ProductHeader(profile: DietProfile) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("먹기 전 신호", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = SignalColors.Ink)
    Text(
      "먹기 전에 알레르기, 매운맛, 식이 제한 위험을 확인합니다.",
      style = MaterialTheme.typography.bodyMedium,
      color = SignalColors.Muted,
    )
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      profile.allergens.forEach { ToneChip("알레르기 $it", SignalTone.Danger) }
      profile.dietTags.forEach { ToneChip(it, SignalTone.Info) }
    }
  }
}

@Composable
private fun SearchAndActionPanel(
  actions: List<QuickAction>,
  query: String,
  onQueryChange: (String) -> Unit,
) {
  SignalCard {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Text("바로 확인", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SignalColors.Ink)
      OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("제품명, 메뉴명, 브랜드 검색") },
        singleLine = true,
        leadingIcon = { Text("검색") },
      )
      actions.forEach { action -> QuickActionRow(action) }
    }
  }
}

@Composable
private fun QuickActionRow(action: QuickAction) {
  val palette = action.state.palette()
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(palette.container)
        .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    StateDot(palette.content)
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(action.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = palette.content)
      Text(action.description, style = MaterialTheme.typography.bodySmall, color = SignalColors.Muted)
    }
    Text("열기", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = palette.content)
  }
}

@Composable
private fun ProfileSummaryCard(profile: DietProfile) {
  SignalCard(container = SignalColors.PrimarySoft, border = SignalColors.Primary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("내 기준", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SignalColors.PrimaryDark)
      Text(
        "우유·대두·새우 알레르기와 매운맛 약함 기준으로 위험 신호를 계산합니다.",
        style = MaterialTheme.typography.bodyMedium,
        color = SignalColors.Ink,
      )
      SpicyToleranceRow(profile.spicyTolerance)
    }
  }
}

@Composable
private fun SectionTitle(title: String, description: String) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SignalColors.Ink)
    Text(description, style = MaterialTheme.typography.bodySmall, color = SignalColors.Muted)
  }
}

@Composable
private fun FoodResultCard(food: FoodSignal, selected: Boolean, onClick: () -> Unit) {
  val tone = food.riskLevel.toTone()
  val palette = tone.palette()
  Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = if (selected) palette.container else SignalColors.Surface),
    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) palette.content else SignalColors.Line),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.Top,
    ) {
      FoodBadge(food.riskLevel)
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SignalColors.Ink, modifier = Modifier.weight(1f))
          Spacer(Modifier.width(8.dp))
          ToneChip(food.riskLevel.label(), tone)
        }
        Text("${food.brand} · ${food.category}", style = MaterialTheme.typography.bodySmall, color = SignalColors.Muted)
        Text(food.summary, style = MaterialTheme.typography.bodyMedium, color = SignalColors.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          food.matchedAllergens.forEach { ToneChip(it, SignalTone.Danger) }
          food.possibleAllergens.forEach { ToneChip("$it 가능", SignalTone.Warning) }
          ToneChip(food.confidence, SignalTone.Info)
        }
      }
    }
  }
}

@Composable
private fun RiskDetailCard(food: FoodSignal) {
  val tone = food.riskLevel.toTone()
  val palette = tone.palette()
  SignalCard(border = palette.content) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FoodBadge(food.riskLevel)
        Column(modifier = Modifier.weight(1f)) {
          Text(food.headline, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = palette.content)
          Text(food.name, style = MaterialTheme.typography.bodyMedium, color = SignalColors.Muted)
        }
      }
      Text(food.summary, style = MaterialTheme.typography.bodyLarge, color = SignalColors.Ink)
      EvidenceGrid(food)
      SpicySignalRow(food.spicyLevel)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("권장 행동", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SignalColors.Ink)
        Text(food.recommendedAction, style = MaterialTheme.typography.bodyMedium, color = SignalColors.Ink)
      }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("대체 메뉴") }
        Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("문의하기") }
      }
    }
  }
}

@Composable
private fun EvidenceGrid(food: FoodSignal) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    EvidenceRow("출처", food.source, SignalTone.Safe)
    EvidenceRow("업데이트", food.updatedAt, SignalTone.Info)
    EvidenceRow("신뢰도", food.confidence, food.riskLevel.toTone())
    EvidenceRow("주의", "정보 없음은 안전 보장이 아닙니다", SignalTone.Warning)
    if (food.cautionTags.isNotEmpty()) {
      Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        food.cautionTags.forEach { ToneChip(it, SignalTone.Warning) }
      }
    }
  }
}

@Composable
private fun EvidenceRow(label: String, value: String, tone: SignalTone) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    ToneChip(label, tone)
    Text(value, style = MaterialTheme.typography.bodySmall, color = SignalColors.Muted, modifier = Modifier.weight(1f))
  }
}

@Composable
private fun SpicySignalRow(level: Int) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text("매운맛 신호", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SignalColors.Ink)
      Text("${level}/5", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = SignalColors.WarningDark)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
      repeat(6) { index ->
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(10.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (index <= level) SignalColors.Warning else SignalColors.Line),
        )
      }
    }
    Text("매운맛 약한 사용자 기준으로 해석합니다.", style = MaterialTheme.typography.bodySmall, color = SignalColors.Muted)
  }
}

@Composable
private fun SpicyToleranceRow(level: Int) {
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Text("매운맛 내성", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SignalColors.PrimaryDark)
    repeat(5) { index ->
      Box(
        modifier =
          Modifier
            .size(width = 26.dp, height = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (index < level) SignalColors.Primary else SignalColors.Line),
      )
    }
    Text("$level/5", style = MaterialTheme.typography.bodySmall, color = SignalColors.Muted)
  }
}

@Composable
private fun StaffPromptCard(prompt: String) {
  SignalCard(container = SignalColors.PrimarySoft, border = SignalColors.Primary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text("직원에게 보여줄 문장", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SignalColors.PrimaryDark)
      Text("“$prompt”", style = MaterialTheme.typography.bodyLarge, color = SignalColors.Ink)
    }
  }
}

@Composable
private fun SafetyPolicyCard() {
  SignalCard(container = SignalColors.WarningSoft, border = SignalColors.Warning) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("표현 정책", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SignalColors.WarningDark)
      Text(
        "이 앱은 의료적 확정 판단을 제공하지 않습니다. 공식 정보와 가능성 정보를 구분하고, 알레르기 위험은 주문 전 확인을 유도합니다.",
        style = MaterialTheme.typography.bodyMedium,
        color = SignalColors.Ink,
      )
    }
  }
}

@Composable
private fun EmptyResultCard(query: String) {
  SignalCard(container = SignalColors.InfoSoft, border = SignalColors.Info) {
    Text("“$query” 검색 결과가 없습니다. 바코드/식품QR 스캔 또는 성분표 촬영을 시도하세요.", color = SignalColors.Ink)
  }
}

@Composable
private fun SignalBottomBar() {
  NavigationBar(containerColor = SignalColors.Surface) {
    listOf("홈", "스캔", "기록", "프로필").forEachIndexed { index, label ->
      NavigationBarItem(
        selected = index == 0,
        onClick = {},
        icon = { StateDot(if (index == 0) SignalColors.Primary else SignalColors.LineStrong) },
        label = { Text(label) },
      )
    }
  }
}

@Composable
private fun FoodBadge(riskLevel: RiskLevel) {
  val palette = riskLevel.toTone().palette()
  Box(
    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(palette.container),
    contentAlignment = Alignment.Center,
  ) {
    Text(riskLevel.shortLabel(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = palette.content)
  }
}

@Composable
private fun ToneChip(label: String, tone: SignalTone) {
  val palette = tone.palette()
  Surface(
    shape = RoundedCornerShape(50),
    color = palette.container,
    border = BorderStroke(1.dp, palette.border),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      StateDot(palette.content)
      Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = palette.content)
    }
  }
}

@Composable
private fun StateDot(color: Color) {
  Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
}

@Composable
private fun SignalCard(
  modifier: Modifier = Modifier,
  container: Color = SignalColors.Surface,
  border: Color = SignalColors.Line,
  content: @Composable () -> Unit,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = container),
    border = BorderStroke(1.dp, border),
  ) {
    Box(modifier = Modifier.padding(16.dp)) { content() }
  }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize().background(SignalColors.Background), contentAlignment = Alignment.Center) {
    Text("먹기 전 신호를 준비하는 중", color = SignalColors.Muted)
  }
}

@Composable
private fun ErrorScreen(throwable: Throwable, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize().background(SignalColors.Background).padding(24.dp), contentAlignment = Alignment.Center) {
    Text("데이터를 불러오지 못했습니다: ${throwable.message}", color = SignalColors.Danger)
  }
}

private fun RiskLevel.toTone(): SignalTone =
  when (this) {
    RiskLevel.High -> SignalTone.Danger
    RiskLevel.Check -> SignalTone.Warning
    RiskLevel.Low -> SignalTone.Safe
  }

private fun RiskLevel.label(): String =
  when (this) {
    RiskLevel.High -> "위험 높음"
    RiskLevel.Check -> "확인 필요"
    RiskLevel.Low -> "주의 낮음"
  }

private fun RiskLevel.shortLabel(): String =
  when (this) {
    RiskLevel.High -> "위험"
    RiskLevel.Check -> "확인"
    RiskLevel.Low -> "낮음"
  }

private fun SignalTone.palette(): TonePalette =
  when (this) {
    SignalTone.Danger -> TonePalette(SignalColors.DangerSoft, SignalColors.Danger, SignalColors.Danger)
    SignalTone.Warning -> TonePalette(SignalColors.WarningSoft, SignalColors.WarningDark, SignalColors.Warning)
    SignalTone.Safe -> TonePalette(SignalColors.PrimarySoft, SignalColors.PrimaryDark, SignalColors.Primary)
    SignalTone.Info -> TonePalette(SignalColors.InfoSoft, SignalColors.Info, SignalColors.Info)
  }

private data class TonePalette(
  val container: Color,
  val content: Color,
  val border: Color,
)

private object SignalColors {
  val Background = Color(0xFFF6F4EC)
  val Surface = Color(0xFFFFFFFF)
  val Ink = Color(0xFF17201C)
  val Muted = Color(0xFF5C655F)
  val Line = Color(0xFFD9D5CA)
  val LineStrong = Color(0xFFB9B2A5)
  val Primary = Color(0xFF2F8F6B)
  val PrimaryDark = Color(0xFF1F674D)
  val PrimarySoft = Color(0xFFE7F4EE)
  val Danger = Color(0xFFD94B4B)
  val DangerSoft = Color(0xFFFDECEC)
  val Warning = Color(0xFFF2B84B)
  val WarningDark = Color(0xFF8A5C00)
  val WarningSoft = Color(0xFFFFF6D9)
  val Info = Color(0xFF4978D4)
  val InfoSoft = Color(0xFFEDF2FF)
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
  BeforeMealSignalTheme {
    MainScreen(
      state =
        FoodSignalScreenState(
          dashboard =
            FoodSignalDashboard(
              profile = DietProfile(listOf("우유", "대두", "새우"), listOf("매운맛 약함"), 1),
              quickActions = emptyList(),
              foods = emptyList(),
              staffPrompt = "알레르기 성분을 확인해 주세요.",
            ),
          query = "",
          filteredFoods = emptyList(),
          selectedFood = null,
        ),
      onQueryChange = {},
      onFoodSelected = {},
    )
  }
}
