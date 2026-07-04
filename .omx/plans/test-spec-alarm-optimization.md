# Test Spec: Alarm Optimization

## Unit Tests

- `uiState_startsWithOnboardingDefaults` should keep reminder defaults enabled.
- Toggling notifications should update reminder enabled state and summary.
- Meal-period toggling should remove and restore a period.
- Meal-period toggling should not allow all periods to be removed.
- Lead-time selection should accept only supported values.

## UI Tests

- Profile tab rendering should show:
  - `식전 알림`
  - the current reminder summary
  - lead-time option copy

## Verification Commands

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

