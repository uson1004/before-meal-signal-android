# Plan: Meal Period Cards

## Requirements Summary

Refactor the meal model and UI so a day can show breakfast, lunch, and dinner as separate cards. Meal information must stay primary on the home screen, and existing allergy/spicy/check-in behavior should keep working.

## RALPLAN-DR Summary

### Principles

- Preserve real meal-period data from NEIS; do not fake grouping in UI.
- Keep the refactor small and reversible.
- Keep meal information above check-in controls.
- Do not add dependencies.
- Verify data mapping and visible home UI.

### Decision Drivers

- Correctness: breakfast/lunch/dinner must be representable from source data.
- Low churn: current ViewModel and UI derive risk from `MealDay.menuItems`.
- Testability: mapper and home rendering must have focused regression tests.

### Viable Options

#### Option A: Add `MealSection` under `MealDay`, keep flattened compatibility

Add meal-period sections to `MealDay`, render those as cards, and keep `MealDay.menuItems` as a derived flatten for existing allergy/spicy/report code.

Pros:
- Smallest safe change across `MainScreenViewModel.kt` and `MainScreen.kt`.
- Keeps current risk calculations working while UI becomes period-aware.
- Easy to test with mixed `조식`/`중식`/`석식` rows.

Cons:
- Flat `menuItems` temporarily hides period context in some summaries.

#### Option B: Replace `menuItems` with period-only data everywhere

Remove flat meal items and update all callers to use sections.

Pros:
- Cleaner long-term domain model.

Cons:
- Larger blast radius across ViewModel, report flow, tests, and samples.
- More chance of accidental behavior changes.

#### Option C: UI-only split

Keep data unchanged and show three cards by copying the same `MealDay.menuItems` into one or more cards.

Pros:
- Smallest UI-only diff.

Cons:
- Incorrect when NEIS has separate breakfast/lunch/dinner rows.
- Does not solve the current single-meal fetch issue.

## ADR

Decision: Choose Option A.

Drivers:
- Current model flattening is used by risk and report logic.
- The UI needs period cards now, not a full domain rewrite.
- NEIS already exposes `MMEAL_SC_NM`, so source grouping should happen in the mapper.

Alternatives considered:
- Replace `menuItems` everywhere: rejected because it expands the refactor without user-visible benefit for this task.
- UI-only cards: rejected because it cannot correctly represent breakfast/dinner.

Why chosen:
- It preserves source period data while keeping existing business logic stable.

Consequences:
- `MealDay.menuItems` remains a compatibility flatten.
- Future work can make risk/report period-aware if needed.

Follow-ups:
- Consider period-aware report targets after the UI card refactor lands.

## Acceptance Criteria

- Home screen shows meal cards for `아침`, `점심`, and `저녁` before the quick check-in section.
- A day with NEIS rows for `조식`, `중식`, and `석식` maps each row into the matching card.
- If a period has no data, its card shows a calm empty message, not blank space.
- Existing allergy/spicy derived values still consider all meals for the selected day.
- Report targets still populate from today’s available menu items.
- Default NEIS requests do not send `MMEAL_SC_CODE`, so mixed `조식`/`중식`/`석식` rows can reach the repository/mapper path.
- `./gradlew testDebugUnitTest lintDebug assembleDebug` passes.

## Implementation Steps

1. Update the data model in `app/src/main/java/com/example/beforemealsignal/data/DataRepository.kt`.
   - Add `MealSection(displayName: String, mealType: String, menuItems: List<MealItem>)`.
   - Change `MealDay` to store `mealSections: List<MealSection>`.
   - Keep a derived `val MealDay.menuItems: List<MealItem>` flatten for compatibility.
   - Keep or derive `mealType` only if needed by current UI copy.

2. Fix NEIS fetch scope in `app/src/main/java/com/example/beforemealsignal/data/NeisApiClient.kt`.
   - Remove the current blank-default behavior that sends `MMEAL_SC_CODE=2`.
   - Build request params without `MMEAL_SC_CODE` by default.
   - Add `MMEAL_SC_CODE` only when `config.mealCode` is explicitly nonblank.
   - This is the chosen strategy; do not add a parallel 1/2/3 multi-fetch path unless a verified API failure proves omission does not return all periods.

3. Group source rows by period in `app/src/main/java/com/example/beforemealsignal/data/NeisMealRepository.kt`.
   - Within each date, group `NeisMealRow` by `mealName`.
   - Order sections as `조식`, `중식`, `석식`.
   - Display labels as `아침`, `점심`, `저녁`.
   - Fallback days should still include three empty sections.
   - `reportTargets()` should flatten all sections for today.

4. Update the home and week UI in `app/src/main/java/com/example/beforemealsignal/ui/main/MainScreen.kt`.
   - Replace `MenuListCard(meal = ...)` on home with period cards.
   - Put meal cards before `FoodSignalInfoCard` and `SignalCheckInCard`.
   - Keep empty period cards compact.
   - Update weekly summary to mention available period labels or keep a compact all-menu summary using the flatten.

5. Update tests.
   - `NeisMealMapperTest.kt`: add a same-date fixture with `조식`, `중식`, `석식` rows and assert section order/labels/items.
   - `NeisMealMapperTest.kt`: assert fallback has three empty sections.
   - Add a focused client/repository test that proves blank `mealCode` does not produce a request containing `MMEAL_SC_CODE=2`; if direct URL testing is awkward, extract the params builder as a small internal helper and test that.
   - Also assert an explicit nonblank `mealCode` still sends `MMEAL_SC_CODE`, so the override path stays protected.
   - `MainScreenTest.kt`: assert home shows `아침`, `점심`, `저녁`, and a lunch menu item before check-in copy.
   - Update all `MealDay(...)` constructors in samples, previews, fallback, and tests to provide `mealSections`.

## Risks and Mitigations

- Risk: NEIS request still returns only lunch.
  - Mitigation: default request omits `MMEAL_SC_CODE`; add a test proving the blank-meal-code request path no longer sends the lunch-only filter.

- Risk: Two representations (`mealSections` and flattened `menuItems`) diverge.
  - Mitigation: make flattened `menuItems` derived, not independently stored.

- Risk: Empty breakfast/dinner cards feel noisy.
  - Mitigation: compact empty copy such as `등록된 급식 정보가 없어요`.

## Verification Steps

- Run `./gradlew testDebugUnitTest`.
- Run `./gradlew lintDebug`.
- Run `./gradlew assembleDebug`.
- Manually inspect the home Compose preview or app screen for card order: 급식 header, 아침/점심/저녁 cards, info, quick check-in.

## Available-Agent-Types Roster

- `executor`: implement bounded Kotlin/Compose refactor.
- `test-engineer`: add mapper/UI regression tests.
- `architect`: review domain model boundary if the mapper shape changes.
- `critic`: final plan or diff review.
- `verifier`: run Gradle checks and inspect evidence.

## Follow-Up Staffing Guidance

Ralph path:
- Use one `executor` lane with high reasoning for `DataRepository.kt`, `NeisApiClient.kt`, `NeisMealRepository.kt`, `MainScreenViewModel.kt`, and `MainScreen.kt`.
- Use one `verifier` lane after implementation for Gradle checks.

Team path:
- Worker 1, `executor`: data model and NEIS mapper/fetch scope.
- Worker 2, `executor`: Compose meal-period card UI.
- Worker 3, `test-engineer`: mapper and UI tests.
- Verification lane, `verifier`: run `testDebugUnitTest lintDebug assembleDebug`.

## Launch Hints

Sequential:

```bash
$ralph .omx/plans/meal-period-cards-plan.md
```

Team:

```bash
$team .omx/plans/meal-period-cards-plan.md
```

## Team Verification Path

- Data worker proves mixed `조식`/`중식`/`석식` rows produce three sections.
- UI worker proves home displays the three period cards before check-in.
- Test worker proves fallback and mixed-row regressions.
- Verifier runs Gradle checks and reports exact commands/results.

## Changelog

- Applied Architect feedback: added NEIS fetch-scope correction before period grouping.
- Kept Option A but made flattened `menuItems` derived to avoid two independent truths.
- Applied Critic feedback: fixed the fetch strategy to omit `MMEAL_SC_CODE` by default and added explicit constructor/test migration steps.
- Applied final Critic note: params tests must cover both blank default and explicit override meal codes.
