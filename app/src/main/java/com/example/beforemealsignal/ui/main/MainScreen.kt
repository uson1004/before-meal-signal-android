package com.example.beforemealsignal.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.beforemealsignal.data.DataRepository
import com.example.beforemealsignal.data.MealDay
import com.example.beforemealsignal.data.MealItem
import com.example.beforemealsignal.data.MealSection
import com.example.beforemealsignal.data.ReportTarget
import com.example.beforemealsignal.data.sampleDashboard
import com.example.beforemealsignal.theme.BeforeMealSignalTheme
import com.example.beforemealsignal.theme.MealDesignTokens
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  dataRepository: DataRepository,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(dataRepository) },
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  when (val current = state) {
    MainScreenUiState.Loading -> LoadingScreen(modifier)
    is MainScreenUiState.Success ->
      MainScreen(
        state = current.state,
        onAllergenToggled = viewModel::onAllergenToggled,
        onSpicyToleranceSelected = viewModel::onSpicyToleranceSelected,
        onStartOnboarding = viewModel::onStartOnboarding,
        onSkipOnboarding = viewModel::onSkipOnboarding,
        onTabSelected = viewModel::onTabSelected,
        onDaySelected = viewModel::onDaySelected,
        onReportSpicySelected = viewModel::onReportSpicySelected,
        onReportAllergenToggled = viewModel::onReportAllergenToggled,
        onPhotoToggle = viewModel::onPhotoToggle,
        onSubmitReport = viewModel::onSubmitReport,
        onEditProfile = viewModel::onEditProfile,
        onNotificationsToggle = viewModel::onNotificationsToggle,
        onReminderMealToggled = viewModel::onReminderMealToggled,
        onReminderLeadSelected = viewModel::onReminderLeadSelected,
        modifier = modifier,
      )
    is MainScreenUiState.Error -> ErrorScreen(current.throwable, modifier)
  }
}

@Composable
internal fun MainScreen(
  state: MealSignalScreenState,
  modifier: Modifier = Modifier,
  onAllergenToggled: (String) -> Unit = {},
  onSpicyToleranceSelected: (Int) -> Unit = {},
  onStartOnboarding: () -> Unit = {},
  onSkipOnboarding: () -> Unit = {},
  onTabSelected: (MealTab) -> Unit = {},
  onDaySelected: (Int) -> Unit = {},
  onReportSpicySelected: (Int) -> Unit = {},
  onReportAllergenToggled: (String) -> Unit = {},
  onPhotoToggle: () -> Unit = {},
  onSubmitReport: () -> Unit = {},
  onEditProfile: () -> Unit = {},
  onNotificationsToggle: () -> Unit = {},
  onReminderMealToggled: (String) -> Unit = {},
  onReminderLeadSelected: (Int) -> Unit = {},
) {
  if (state.local.showOnboarding) {
    OnboardingScreen(
      state = state,
      onAllergenToggled = onAllergenToggled,
      onSpicyToleranceSelected = onSpicyToleranceSelected,
      onStart = onStartOnboarding,
      onSkip = onSkipOnboarding,
      modifier = modifier,
    )
    return
  }

  Scaffold(
    containerColor = MealColors.Background,
    bottomBar = { MealBottomBar(activeTab = state.local.activeTab, onTabSelected = onTabSelected) },
    modifier = modifier.fillMaxSize(),
  ) { innerPadding ->
    when (state.local.activeTab) {
      MealTab.Home ->
        HomeScreen(
          state = state,
          onRateClick = { onTabSelected(MealTab.Report) },
          modifier = Modifier.padding(innerPadding),
        )
      MealTab.Week ->
        WeekScreen(
          state = state,
          onDaySelected = onDaySelected,
          modifier = Modifier.padding(innerPadding),
        )
      MealTab.Report ->
        ReportScreen(
          state = state,
          onSpicySelected = onReportSpicySelected,
          onAllergenToggled = onReportAllergenToggled,
          onPhotoToggle = onPhotoToggle,
          onSubmit = onSubmitReport,
          modifier = Modifier.padding(innerPadding),
        )
      MealTab.Profile ->
        ProfileScreen(
          state = state,
          onEditProfile = onEditProfile,
          onNotificationsToggle = onNotificationsToggle,
          onReminderMealToggled = onReminderMealToggled,
          onReminderLeadSelected = onReminderLeadSelected,
          modifier = Modifier.padding(innerPadding),
        )
    }
  }
}

@Composable
private fun OnboardingScreen(
  state: MealSignalScreenState,
  onAllergenToggled: (String) -> Unit,
  onSpicyToleranceSelected: (Int) -> Unit,
  onStart: () -> Unit,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().mealBackdrop(),
    contentPadding = PaddingValues(24.dp),
    verticalArrangement = Arrangement.spacedBy(22.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    item { NotchSpacer() }
    item { Mascot(size = 82, color = MealColors.Coral) }
    item {
      Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("반가워요!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MealColors.Ink)
        Text(
          "알레르기가 있는 음식을 골라주세요. 나중에 바꿀 수 있어요.",
          style = MaterialTheme.typography.bodyLarge,
          color = MealColors.Muted,
          textAlign = TextAlign.Center,
        )
      }
    }
    item {
      SectionBlock(title = "자주 묻는 알레르기") {
        state.dashboard.allergenOptions.chunked(4).forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            row.forEach { allergen ->
              SelectChip(
                label = allergen,
                selected = allergen in state.selectedAllergens,
                onClick = { onAllergenToggled(allergen) },
              )
            }
          }
        }
      }
    }
    item {
      SectionBlock(title = "매운맛 내성") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          listOf(0, 1, 2).forEach { level ->
            SelectChip(
              label = spicyToleranceLabel(level),
              selected = state.spicyTolerance == level,
              onClick = { onSpicyToleranceSelected(level) },
              modifier = Modifier.weight(1f),
              singleChoice = true,
            )
          }
        }
      }
    }
    item { ShadowButton("시작하기", onClick = onStart, container = MealColors.Green, shadow = MealColors.GreenDark) }
    item { SecondaryActionButton("나중에 설정할게요", onClick = onSkip) }
  }
}

@Composable
private fun HomeScreen(
  state: MealSignalScreenState,
  onRateClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var bodyIndex by remember { mutableIntStateOf(1) }
  var feelingIndex by remember { mutableIntStateOf(0) }
  var urgeIndex by remember { mutableIntStateOf(0) }
  var hungerIndex by remember { mutableIntStateOf(1) }

  LazyColumn(
    modifier = modifier.fillMaxSize().mealBackdrop(),
    contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        BadgePill("${state.streakDays}일 연속 확인", MealColors.GreenSoft, MealColors.GreenDark, "✓")
        if (state.todayEstimated) BadgePill("추정 정보 포함", MealColors.Surface, MealColors.YellowDark, "i")
      }
    }
    item { SectionTitle("오늘 급식") }
    item { MealPeriodPager(meal = state.todayMeal, selectedAllergens = state.selectedAllergens) }
    item { FoodSignalInfoCard(state) }
    item {
      SignalCheckInCard(
        bodyIndex = bodyIndex,
        onBodySelected = { bodyIndex = it },
        feelingIndex = feelingIndex,
        onFeelingSelected = { feelingIndex = it },
        urgeIndex = urgeIndex,
        onUrgeSelected = { urgeIndex = it },
        hungerIndex = hungerIndex,
        onHungerSelected = { hungerIndex = it },
      )
    }
    item { ShadowButton("체크 완료하고 기록하기", onClick = onRateClick, container = MealColors.Green, shadow = MealColors.GreenDark) }
  }
}

@Composable
private fun WeekScreen(
  state: MealSignalScreenState,
  onDaySelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().mealBackdrop(),
    contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("이번 주 급식", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MealColors.Ink)
        Text("${state.dashboard.weekLabel} · ${state.dashboard.sourceLabel}", style = MaterialTheme.typography.bodyMedium, color = MealColors.Muted)
      }
    }
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MealDesignTokens.Radius.Card),
        color = MealColors.YellowSoft,
        shadowElevation = MealDesignTokens.Depth.Card,
        border = BorderStroke(1.dp, MealColors.Yellow),
      ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Text("${state.streakDays}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MealColors.YellowDark)
          Column {
            Text("일 연속 확인 중!", fontWeight = FontWeight.ExtraBold, color = MealColors.YellowDark)
            Text("이번 주도 매일 체크해봐요", style = MaterialTheme.typography.bodySmall, color = MealColors.YellowDark)
          }
        }
      }
    }
    itemsIndexed(state.dashboard.weekMeals) { index, meal ->
      TimelineMealCard(
        meal = meal,
        selected = meal == state.selectedMeal,
        selectedAllergens = state.selectedAllergens,
        onClick = { onDaySelected(index) },
      )
    }
  }
}

@Composable
private fun ReportScreen(
  state: MealSignalScreenState,
  onSpicySelected: (Int) -> Unit,
  onAllergenToggled: (String) -> Unit,
  onPhotoToggle: () -> Unit,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val target = state.dashboard.reportTargets.firstOrNull() ?: emptyReportTarget
  LazyColumn(
    modifier = modifier.fillMaxSize().mealBackdrop(),
    contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("오늘 급식 평가하기", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MealColors.Ink)
        Text("매운맛 체감을 알려주면 다른 친구들에게도 도움이 돼요.", style = MaterialTheme.typography.bodyMedium, color = MealColors.Muted)
      }
    }
    item { ReportTargetCard(target = target, rating = state.local.reportSpicyRating, onSpicySelected = onSpicySelected) }
    item {
      SectionCard {
        SectionTitle("알레르기 성분 제보", compact = true)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          listOf("계란", "새우", "우유", "없음").forEach { allergen ->
            SelectChip(
              label = allergen,
              selected = allergen in state.local.reportAllergens,
              onClick = { onAllergenToggled(allergen) },
            )
          }
        }
      }
    }
    item {
      DashedActionCard(
        label = if (state.local.photoAttached) "사진 첨부됨" else "사진으로 제보하기 (선택)",
        onClick = onPhotoToggle,
      )
    }
    item {
      Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MealColors.GreenSoft) {
        Text(
          if (state.local.reportSubmitted) "제보가 저장됐어요. 연속 확인 스트릭이 1 늘었어요."
          else "제보하면 연속 확인 스트릭이 유지돼요",
          modifier = Modifier.padding(16.dp),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MealColors.GreenDark,
        )
      }
    }
    item { ShadowButton("제보 보내기", onClick = onSubmit, container = MealColors.Green, shadow = MealColors.GreenDark) }
  }
}

@Composable
private fun ProfileScreen(
  state: MealSignalScreenState,
  onEditProfile: () -> Unit,
  onNotificationsToggle: () -> Unit,
  onReminderMealToggled: (String) -> Unit,
  onReminderLeadSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().mealBackdrop(),
    contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    item { Mascot(size = 86, color = MealColors.Navy) }
    item {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(state.dashboard.profile.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MealColors.Ink)
        Text(state.dashboard.profile.classLabel, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MealColors.Muted)
      }
    }
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard("${state.streakDays}", "연속 확인", Modifier.weight(1f))
        StatCard("${state.reportCount}", "제보 횟수", Modifier.weight(1f))
        StatCard("${state.selectedAllergens.size}", "알레르기", Modifier.weight(1f))
      }
    }
    item {
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("내 알레르기", compact = true)
        ProfileTable(state)
      }
    }
    item {
      ReminderSettingsCard(
        state = state,
        onNotificationsToggle = onNotificationsToggle,
        onMealToggled = onReminderMealToggled,
        onLeadSelected = onReminderLeadSelected,
      )
    }
    item { ShadowButton("알레르기 정보 수정", onClick = onEditProfile, container = MealColors.Coral, shadow = MealColors.CoralDark) }
  }
}

@Composable
private fun SignalCheckInCard(
  bodyIndex: Int,
  onBodySelected: (Int) -> Unit,
  feelingIndex: Int,
  onFeelingSelected: (Int) -> Unit,
  urgeIndex: Int,
  onUrgeSelected: (Int) -> Unit,
  hungerIndex: Int,
  onHungerSelected: (Int) -> Unit,
) {
  SectionCard {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      SectionTitle("빠른 체크인", compact = true)
      Text("정답 없이 지금 상태만 가볍게 고르면 돼요.", style = MaterialTheme.typography.bodyMedium, color = MealColors.Muted)
    }
    SignalChoiceRow("몸 상태", listOf("가벼워요", "보통이에요", "예민해요"), bodyIndex, onBodySelected)
    SignalChoiceRow("감정", listOf("차분해요", "급해요", "복잡해요"), feelingIndex, onFeelingSelected)
    SignalChoiceRow("충동", listOf("천천히", "빠르게", "확인 더"), urgeIndex, onUrgeSelected)
    SignalChoiceRow("배고픔", listOf("조금", "적당해요", "많아요"), hungerIndex, onHungerSelected)
  }
}

@Composable
private fun SignalChoiceRow(
  title: String,
  options: List<String>,
  selectedIndex: Int,
  onSelected: (Int) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MealColors.Ink)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      options.forEachIndexed { index, label ->
        SelectChip(
          label = label,
          selected = selectedIndex == index,
          onClick = { onSelected(index) },
          modifier = Modifier.weight(1f),
          singleChoice = true,
        )
      }
    }
  }
}

@Composable
private fun FoodSignalInfoCard(state: MealSignalScreenState) {
  val allergenText =
    if (state.todayMatchedAllergens.isEmpty()) "등록한 알레르기와 바로 겹치는 표시는 없어요."
    else "${state.riskMenuName ?: "오늘 메뉴"}에 ${state.todayMatchedAllergens.joinToString("·")} 표시가 있어요."

  SectionCard {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      SectionTitle("오늘 확인할 정보", compact = true)
      Text("안전을 단정하지 않고, 먹기 전 참고할 신호만 보여줘요.", style = MaterialTheme.typography.bodyMedium, color = MealColors.Muted)
    }
    InfoRow(label = "알레르기", value = allergenText, tone = if (state.todayMatchedAllergens.isEmpty()) TagTone.Green else TagTone.Danger)
    InfoRow(label = "매운맛", value = "${spicyLevelLabel(state.todaySpicyLevel)} · 내 기준 ${spicyToleranceLabel(state.spicyTolerance)}", tone = TagTone.Yellow)
    if (state.todayEstimated) {
      InfoRow(label = "출처", value = "일부 메뉴는 추정 정보라 메뉴명을 한 번 더 확인해요.", tone = TagTone.Neutral)
    }
  }
}

@Composable
private fun InfoRow(label: String, value: String, tone: TagTone) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
    Tag(label = label, tone = tone)
    Text(
      value,
      modifier = Modifier.weight(1f).padding(top = 5.dp),
      style = MaterialTheme.typography.bodyMedium,
      color = MealColors.Ink,
    )
  }
}

@Composable
private fun MealPeriodPager(meal: MealDay, selectedAllergens: Set<String>) {
  val sections = meal.mealSections.ifEmpty { listOf(MealSection("점심", "중식", meal.menuItems)) }
  val lunchPage = sections.indexOfFirst { it.mealType == "중식" && it.menuItems.isNotEmpty() }
  val firstServedPage = sections.indexOfFirst { it.menuItems.isNotEmpty() }
  val initialPage = lunchPage.takeIf { it >= 0 } ?: firstServedPage.takeIf { it >= 0 } ?: 0
  val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { sections.size })
  val scope = rememberCoroutineScope()

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      sections.forEachIndexed { index, section ->
        val selected = pagerState.currentPage == index
        Surface(
          modifier =
            Modifier
              .weight(1f)
              .height(38.dp)
              .clickable { scope.launch { pagerState.animateScrollToPage(index) } },
          shape = RoundedCornerShape(MealDesignTokens.Radius.Pill),
          color = if (selected) MealColors.GreenSoft else MealColors.Surface,
          border = BorderStroke(1.dp, if (selected) MealColors.Green else MealColors.Line),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              section.displayName,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = if (selected) MealColors.GreenDark else MealColors.Muted,
            )
          }
        }
      }
    }
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxWidth(),
      pageSpacing = 12.dp,
      contentPadding = PaddingValues(horizontal = 2.dp),
    ) { page ->
      MealPeriodCard(section = sections[page], selectedAllergens = selectedAllergens)
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      sections.forEachIndexed { index, _ ->
        Spacer(
          modifier =
            Modifier
              .padding(horizontal = 3.dp)
              .size(width = if (pagerState.currentPage == index) 18.dp else 7.dp, height = 7.dp)
              .clip(RoundedCornerShape(MealDesignTokens.Radius.Pill))
              .background(if (pagerState.currentPage == index) MealColors.Green else MealColors.Line),
        )
      }
    }
  }
}

@Composable
private fun MealPeriodCard(section: MealSection, selectedAllergens: Set<String>) {
  SectionCard(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Column {
        Text(section.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MealColors.Ink)
        Text(section.mealType, style = MaterialTheme.typography.bodySmall, color = MealColors.Muted)
      }
      Tag(label = "${section.menuItems.size}개", tone = if (section.menuItems.isEmpty()) TagTone.Neutral else TagTone.Green)
    }
    if (section.menuItems.isEmpty()) {
      Box(modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp), contentAlignment = Alignment.CenterStart) {
        Text("등록된 급식 정보가 없어요.", style = MaterialTheme.typography.bodyMedium, color = MealColors.Muted)
      }
    } else {
      section.menuItems.forEachIndexed { index, item ->
        MenuRow(item = item, selectedAllergens = selectedAllergens)
        if (index != section.menuItems.lastIndex) DividerLine()
      }
    }
  }
}

@Composable
private fun MenuRow(item: MealItem, selectedAllergens: Set<String>) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(item.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MealColors.Ink)
    MenuStatusChip(item = item, selectedAllergens = selectedAllergens)
  }
}

@Composable
private fun TimelineMealCard(
  meal: MealDay,
  selected: Boolean,
  selectedAllergens: Set<String>,
  onClick: () -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
    TimelineMarker(meal = meal, selected = selected)
    Surface(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(MealDesignTokens.Radius.Control),
      color = if (selected || meal.isToday) MealColors.SurfaceRaised else Color.Transparent,
      shadowElevation = if (selected || meal.isToday) MealDesignTokens.Depth.Surface else 0.dp,
      border = BorderStroke(1.dp, if (selected || meal.isToday) MealColors.Line else Color.Transparent),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(meal.dateLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = if (meal.isToday) MealColors.Coral else MealColors.Muted)
        Text(
          meal.menuItems.joinToString(" · ") { it.name },
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
          color = MealColors.Ink,
        )
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          meal.matchedAllergens(selectedAllergens).forEach { Tag(label = it, tone = TagTone.Danger) }
          if (meal.matchedAllergens(selectedAllergens).isEmpty()) Tag(label = "표시없음", tone = TagTone.Green)
          if ((meal.menuItems.maxOfOrNull { it.spicyLevel } ?: 0) > 0) Tag(label = spicyLevelLabel(meal.menuItems.maxOf { it.spicyLevel }), tone = TagTone.Yellow)
          if (meal.menuItems.any { it.isEstimated }) Tag(label = "추정", tone = TagTone.Yellow)
        }
      }
    }
  }
}

@Composable
private fun TimelineMarker(meal: MealDay, selected: Boolean) {
  val color =
    when {
      meal.isToday -> MealColors.Coral
      selected -> MealColors.Green
      else -> MealColors.Surface
    }
  val contentColor = if (meal.isToday || selected) Color.White else MealColors.Muted
  Surface(
    modifier = Modifier.size(58.dp),
    shape = CircleShape,
    color = color,
    shadowElevation = if (meal.isToday || selected) MealDesignTokens.Depth.Button else 0.dp,
    border = BorderStroke(2.dp, if (meal.isToday || selected) color else MealColors.Line),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(meal.dayBadge, fontWeight = FontWeight.ExtraBold, color = contentColor)
    }
  }
}

@Composable
private fun ReportTargetCard(
  target: ReportTarget,
  rating: Int,
  onSpicySelected: (Int) -> Unit,
) {
  SectionCard {
    Text("${target.menuName} - 체감 매운맛", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MealColors.Ink)
    Text(target.description, style = MaterialTheme.typography.bodyMedium, color = MealColors.Muted)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      (0..4).forEach { level ->
        NumberChoice(number = level, selected = rating == level, onClick = { onSpicySelected(level) }, modifier = Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun ProfileTable(state: MealSignalScreenState) {
  SectionCard(contentPadding = PaddingValues(0.dp)) {
    val rows =
      state.selectedAllergens.map { it to "등록됨" } +
        listOf("매운맛 내성" to spicyToleranceLabel(state.spicyTolerance))

    rows.forEachIndexed { index, row ->
      Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(row.first, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MealColors.Ink)
        Tag(label = row.second, tone = if (row.second == "등록됨") TagTone.Danger else TagTone.Neutral)
      }
      if (index != rows.lastIndex) DividerLine()
    }
  }
}

@Composable
private fun ReminderSettingsCard(
  state: MealSignalScreenState,
  onNotificationsToggle: () -> Unit,
  onMealToggled: (String) -> Unit,
  onLeadSelected: (Int) -> Unit,
) {
  SectionCard {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      SectionTitle("식전 알림", compact = true)
      Tag(label = state.reminderSummary, tone = if (state.local.notificationsEnabled) TagTone.Green else TagTone.Neutral)
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      reminderMealPeriodOptions.forEach { mealPeriod ->
        SelectChip(
          label = mealPeriod,
          selected = mealPeriod in state.local.reminderSettings.mealPeriods,
          onClick = { onMealToggled(mealPeriod) },
          modifier = Modifier.weight(1f),
        )
      }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      reminderLeadOptions.forEach { minutes ->
        SelectChip(
          label = "${minutes}분 전",
          selected = state.local.reminderSettings.leadMinutes == minutes,
          onClick = { onLeadSelected(minutes) },
          modifier = Modifier.weight(1f),
          singleChoice = true,
        )
      }
    }
    SecondaryActionButton(
      label = if (state.local.notificationsEnabled) "알림 끄기" else "알림 켜기",
      onClick = onNotificationsToggle,
    )
  }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.height(86.dp),
    shape = RoundedCornerShape(MealDesignTokens.Radius.Small),
    color = MealColors.SurfaceRaised,
    shadowElevation = MealDesignTokens.Depth.Surface,
    border = BorderStroke(1.dp, MealColors.Line),
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
      Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MealColors.CoralDark)
      Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = MealColors.Muted)
    }
  }
}

@Composable
private fun SectionBlock(title: String, content: @Composable ColumnScope.() -> Unit) {
  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MealColors.Muted)
    content()
  }
}

@Composable
private fun SectionCard(
  contentPadding: PaddingValues = PaddingValues(18.dp),
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(MealDesignTokens.Radius.Card),
    color = MealColors.SurfaceRaised,
    tonalElevation = MealDesignTokens.Depth.Surface,
    shadowElevation = MealDesignTokens.Depth.Card,
    border = BorderStroke(1.dp, MealColors.Line),
  ) {
    Column(modifier = Modifier.padding(contentPadding), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
  }
}

@Composable
private fun SectionTitle(title: String, compact: Boolean = false) {
  Text(
    title,
    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.ExtraBold,
    color = MealColors.Ink,
  )
}

@Composable
private fun SelectChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  singleChoice: Boolean = false,
) {
  val background = if (selected) MealColors.GreenSoft else MealColors.Surface
  val border = if (selected) MealColors.Green else MealColors.Line
  val content = if (selected) MealColors.GreenDark else MealColors.Ink
  val selectionModifier =
    modifier.height(54.dp).let {
      if (singleChoice) {
        it.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
      } else {
        it.toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
      }
    }
  Surface(
    modifier = selectionModifier,
    shape = RoundedCornerShape(MealDesignTokens.Radius.Control),
    color = background,
    shadowElevation = if (selected) MealDesignTokens.Depth.Surface else 0.dp,
    border = BorderStroke(1.dp, border),
  ) {
    Box(modifier = Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
      Text(
        label,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = content,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun NumberChoice(
  number: Int,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.height(54.dp).clickable(onClick = onClick),
    shape = RoundedCornerShape(MealDesignTokens.Radius.Small),
    color = if (selected) MealColors.Yellow else MealColors.SurfaceRaised,
    shadowElevation = if (selected) MealDesignTokens.Depth.Button else MealDesignTokens.Depth.Surface,
    border = BorderStroke(1.dp, if (selected) MealColors.YellowDark else MealColors.Line),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text("$number", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = if (selected) MealColors.Ink else MealColors.Muted)
    }
  }
}

@Composable
private fun ShadowButton(
  label: String,
  onClick: () -> Unit,
  container: Color,
  shadow: Color,
) {
  Box(modifier = Modifier.fillMaxWidth().height(76.dp).clickable(onClick = onClick)) {
    val buttonShape = RoundedCornerShape(MealDesignTokens.Radius.Control)
    Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(62.dp).clip(buttonShape).background(shadow))
    Box(
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .fillMaxWidth()
          .height(62.dp)
          .shadow(MealDesignTokens.Depth.Button, buttonShape, clip = false)
          .clip(buttonShape)
          .background(container),
      contentAlignment = Alignment.Center,
    ) {
      Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
  }
}

@Composable
private fun SecondaryActionButton(label: String, onClick: () -> Unit) {
  Surface(
    modifier = Modifier.fillMaxWidth().height(64.dp).clickable(onClick = onClick),
    shape = RoundedCornerShape(MealDesignTokens.Radius.Control),
    color = MealColors.SurfaceRaised,
    shadowElevation = MealDesignTokens.Depth.Surface,
    border = BorderStroke(1.dp, MealColors.Line),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MealColors.Muted)
    }
  }
}

@Composable
private fun DashedActionCard(label: String, onClick: () -> Unit) {
  Surface(
    modifier = Modifier.fillMaxWidth().height(82.dp).clickable(onClick = onClick),
    shape = RoundedCornerShape(MealDesignTokens.Radius.Control),
    color = MealColors.Background,
    shadowElevation = MealDesignTokens.Depth.Surface,
    border = BorderStroke(1.dp, MealColors.Line),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MealColors.Muted)
    }
  }
}

@Composable
private fun BadgePill(label: String, background: Color, content: Color, icon: String) {
  Surface(
    shape = RoundedCornerShape(MealDesignTokens.Radius.Pill),
    color = background,
    shadowElevation = MealDesignTokens.Depth.Surface,
    border = BorderStroke(1.dp, MealColors.Line),
  ) {
    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
      Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = MealColors.Yellow) {
        Box(contentAlignment = Alignment.Center) { Text(icon, fontWeight = FontWeight.ExtraBold, color = content) }
      }
      Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = content)
    }
  }
}

@Composable
private fun MenuStatusChip(item: MealItem, selectedAllergens: Set<String>) {
  val label = item.statusLabel(selectedAllergens)
  val tone =
    when {
      item.allergens.any { it in selectedAllergens } -> TagTone.Danger
      label == "추정" -> TagTone.Yellow
      label.contains("매움") -> TagTone.Yellow
      else -> TagTone.Green
    }
  Tag(label = label, tone = tone)
}

@Composable
private fun Tag(label: String, tone: TagTone) {
  val colors = tone.colors()
  Surface(shape = RoundedCornerShape(24.dp), color = colors.background) {
    Text(
      label,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.ExtraBold,
      color = colors.content,
    )
  }
}

@Composable
private fun Mascot(size: Int, color: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier.size(size.dp).shadow(MealDesignTokens.Depth.Floating, CircleShape, clip = false)) {
    val radius = this.size.minDimension / 2f
    drawCircle(color = color, radius = radius)
    drawCircle(color = Color.White, radius = radius * 0.13f, center = Offset(radius * 0.65f, radius * 0.72f))
    drawCircle(color = Color.White, radius = radius * 0.13f, center = Offset(radius * 1.35f, radius * 0.72f))
    drawCircle(color = MealColors.Ink, radius = radius * 0.055f, center = Offset(radius * 0.65f, radius * 0.72f))
    drawCircle(color = MealColors.Ink, radius = radius * 0.055f, center = Offset(radius * 1.35f, radius * 0.72f))
    drawCircle(color = Color.White.copy(alpha = 0.25f), radius = radius * 0.13f, center = Offset(radius * 0.35f, radius * 1.05f))
    drawCircle(color = Color.White.copy(alpha = 0.25f), radius = radius * 0.13f, center = Offset(radius * 1.65f, radius * 1.05f))
    drawArc(
      color = Color.White,
      startAngle = 25f,
      sweepAngle = 130f,
      useCenter = false,
      topLeft = Offset(radius * 0.63f, radius * 0.92f),
      size = Size(radius * 0.75f, radius * 0.55f),
      style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round),
    )
  }
}

@Composable
private fun MealBottomBar(activeTab: MealTab, onTabSelected: (MealTab) -> Unit) {
  Surface(
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    shape = RoundedCornerShape(MealDesignTokens.Radius.Sheet),
    color = MealColors.SurfaceRaised,
    shadowElevation = MealDesignTokens.Depth.Floating,
    border = BorderStroke(1.dp, MealColors.Line),
  ) {
    NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp, modifier = Modifier.height(76.dp)) {
      MealTab.entries.forEach { tab ->
        val selected = activeTab == tab
        NavigationBarItem(
          selected = selected,
          onClick = { onTabSelected(tab) },
          colors =
            NavigationBarItemDefaults.colors(
              indicatorColor = Color.Transparent,
              selectedIconColor = MealColors.Coral,
              selectedTextColor = MealColors.Navy,
              unselectedIconColor = MealColors.Muted,
              unselectedTextColor = MealColors.Muted,
            ),
          icon = {
            Box(
              modifier =
                Modifier
                  .size(if (selected) 30.dp else 24.dp)
                  .clip(CircleShape)
                  .background(if (selected) MealColors.Coral else MealColors.Line),
            )
          },
          label = { Text(tab.label, fontWeight = FontWeight.ExtraBold) },
        )
      }
    }
  }
}

@Composable
private fun DividerLine() {
  Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(MealColors.Line))
}

private fun Modifier.mealBackdrop(): Modifier =
  background(
    Brush.verticalGradient(
      colors = listOf(MealColors.Background, MealColors.BackgroundDeep, MealColors.Background),
    ),
  )

@Composable
private fun NotchSpacer() {
  Box(modifier = Modifier.width(112.dp).height(30.dp).clip(RoundedCornerShape(20.dp)).background(MealColors.Ink))
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize().mealBackdrop(), contentAlignment = Alignment.Center) {
    Text("먹기 전 신호를 준비하는 중", color = MealColors.Muted)
  }
}

@Composable
private fun ErrorScreen(throwable: Throwable, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize().mealBackdrop().padding(24.dp), contentAlignment = Alignment.Center) {
    Text("데이터를 불러오지 못했습니다: ${throwable.message}", color = MealColors.Red)
  }
}

private fun spicyToleranceLabel(level: Int): String =
  when (level) {
    0 -> "잘 못 먹음"
    1 -> "보통"
    else -> "잘 먹음"
  }

private fun spicyLevelLabel(level: Int): String =
  when {
    level <= 0 -> "안매움"
    level == 1 -> "약간매움"
    level == 2 -> "보통 매움"
    level == 3 -> "매움"
    else -> "매우 매움"
  }

private enum class TagTone {
  Danger,
  Green,
  Yellow,
  Neutral,
}

private data class TagColors(val background: Color, val content: Color)

private fun TagTone.colors(): TagColors =
  when (this) {
    TagTone.Danger -> TagColors(MealColors.RedSoft, MealColors.Red)
    TagTone.Green -> TagColors(MealColors.GreenSoft, MealColors.GreenDark)
    TagTone.Yellow -> TagColors(MealColors.YellowSoft, MealColors.YellowDark)
    TagTone.Neutral -> TagColors(MealColors.SurfaceAlt, MealColors.Muted)
  }

private val emptyReportTarget = ReportTarget("급식 정보 없음", "제보할 급식 데이터가 아직 없어요.")

private object MealColors {
  val Background = MealDesignTokens.Colors.Canvas
  val BackgroundDeep = MealDesignTokens.Colors.CanvasDeep
  val Surface = MealDesignTokens.Colors.Surface
  val SurfaceRaised = MealDesignTokens.Colors.SurfaceRaised
  val SurfaceAlt = MealDesignTokens.Colors.SurfacePressed
  val Ink = MealDesignTokens.Colors.Ink
  val Muted = MealDesignTokens.Colors.Muted
  val Line = MealDesignTokens.Colors.Line
  val Coral = MealDesignTokens.Colors.Coral
  val CoralDark = MealDesignTokens.Colors.CoralDeep
  val Navy = MealDesignTokens.Colors.Navy
  val NavyDeep = MealDesignTokens.Colors.NavyDeep
  val NavySoft = MealDesignTokens.Colors.NavySoft
  val Red = MealDesignTokens.Colors.Danger
  val RedDark = MealDesignTokens.Colors.DangerDeep
  val RedSoft = MealDesignTokens.Colors.DangerSoft
  val Yellow = MealDesignTokens.Colors.Amber
  val YellowDark = MealDesignTokens.Colors.AmberDeep
  val YellowSoft = MealDesignTokens.Colors.AmberSoft
  val Green = MealDesignTokens.Colors.Mint
  val GreenDark = MealDesignTokens.Colors.MintDeep
  val GreenSoft = MealDesignTokens.Colors.MintSoft
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
  BeforeMealSignalTheme {
    MainScreen(
      state =
        MealSignalScreenState(
          dashboard = sampleDashboard,
          local = MealPrototypeState(showOnboarding = false),
        ),
    )
  }
}
