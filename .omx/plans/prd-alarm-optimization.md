# PRD: Alarm Optimization

## Goal

Make the existing notification prototype more useful by turning the plain on/off state into configurable meal reminder settings.

## Scope

- Add reminder settings to local prototype state:
  - enabled/disabled
  - target meal periods: 아침, 점심, 저녁
  - lead time options: 10, 20, 30 minutes before meal
- Show reminder settings in the profile screen.
- Derive a concise reminder summary from enabled settings and today's served meal periods.
- Keep at least one meal period selected when reminders are enabled.

## Out Of Scope

- OS-level scheduling with `AlarmManager` or `WorkManager`.
- Runtime notification permission flow.
- Persistent storage.
- Server-side meal-time configuration.

## Acceptance Criteria

- Profile screen shows a 식전 알림 section.
- Users can toggle reminders on/off.
- Users can choose meal periods and lead time.
- The summary excludes meal periods that do not have meal data today.
- Unit tests cover reminder defaults, toggling, and validation.
- Existing build, lint, and unit tests pass.

