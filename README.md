# 먹기 전 신호 Android

음식을 먹기 전에 알레르기, 매운맛, 식이 제한 위험을 확인하는 Android MVP입니다.

## 현재 범위

- 사용자 기준 프로필: 알레르기, 매운맛 내성, 식이 태그
- 제품/메뉴 검색: 로컬 데모 데이터 기반 필터링
- 위험 신호 카드: 위험 높음, 확인 필요, 주의 낮음
- 상세 근거: 출처, 업데이트일, 신뢰도, 정보 한계
- 매운맛 신호: 0-5단계 표시
- 직원 확인 문장: 주문 전 확인용 문구
- 안전 표현 정책: 의료적 확정 판단 금지, 정보 없음은 안전 보장 아님

## Android 스택

- Kotlin
- Jetpack Compose
- Material 3
- Navigation 3
- Gradle / Android Gradle Plugin

## 로컬 검증

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

## NEIS 로컬 설정

NEIS 인증키와 학교 코드는 Git에 올라가지 않는 `local.properties`에만 둡니다.

```properties
NEIS_API_KEY=발급받은_인증키
NEIS_OFFICE_CODE=B10
NEIS_SCHOOL_CODE=7010536
NEIS_MEAL_CODE=2
```

값이 비어 있어도 앱은 빌드되며, 실행 시 NEIS 설정 필요 상태로 빈 주간 식단을 표시합니다.

## APK 산출물

빌드 후 다음 파일이 생성됩니다.

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release-unsigned.apk`

`release` APK는 아직 unsigned 상태입니다. Play Store 업로드에는 정식 `applicationId`, 업로드 키/서명 설정, Play Console 앱 등록 정보가 추가로 필요합니다.

## 데이터 정책 메모

알레르기 정보는 생명과 직결될 수 있으므로 앱은 안전을 단정하지 않습니다. 공식 정보, 브랜드 제공 정보, 사용자 제보, 메뉴명 기반 추정은 서로 다른 신뢰도로 표시되어야 하며, 사용자가 주문 전 확인하도록 유도합니다.
