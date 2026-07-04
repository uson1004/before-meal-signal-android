# Alarm Optimization Context

Task: `$ralph 알람 최적화 진행해줘`

Desired outcome: improve the current notification prototype so users can see and adjust meal reminder timing and meal-period targets before release.

Known facts:
- The app currently has only `MealPrototypeState.notificationsEnabled`.
- There is no `AlarmManager`, `WorkManager`, notification permission flow, receiver, channel, or persistence layer.
- Meal data now exposes breakfast, lunch, and dinner sections.
- The repository has no `.omx/plans/prd-*.md` or `.omx/plans/test-spec-*.md` files yet, so Ralph execution needs planning artifacts first.
- Ouroboros interview failed because it expected `/Users/yuseob/.nvm/versions/node/v22.21.1/bin/codex`, while Codex exists at `/Users/yuseob/.local/bin/codex`.

Constraints:
- Do not add dependencies for this small pass.
- Keep the change testable with local unit/UI tests.
- Avoid pretending OS-level notification scheduling exists until permission, persistence, and background scheduling are designed.

Likely touchpoints:
- `MainScreenViewModel.kt`
- `MainScreen.kt`
- `MainScreenViewModelTest.kt`
- `MainScreenTest.kt`

