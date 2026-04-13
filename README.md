# Outspire Android

A native Android port of [Outspire](https://github.com/Computerization/Outspire) (iOS), built against the real TSIMS backend.

- **Status**: working demo against production TSIMS — login, timetable, scores, and CAS all hit live endpoints.
- **Stack**: Kotlin 2.0 · Jetpack Compose · Material 3 · Hilt · Navigation-Compose · Ktor (OkHttp engine) · kotlinx-serialization · kotlinx-datetime · Jsoup
- **minSdk**: 26 (Android 8.0) · **targetSdk**: 35

## Run it

```bash
./gradlew assembleDebug
./gradlew test
```

Or open the project in Android Studio (Koala+), select `app`, and run on an emulator or device.

## Implemented

**Auth & session**
- TSIMS login with real credentials (`/Home/Login` → `/Home/GetMenu` session verify → `/Home/StudentInfo` profile scrape)
- EncryptedSharedPreferences for credentials + cookie jar + cached state
- Auto-retry on 302/401 via `AuthService.withAuthRetry`
- Live logout that clears cookies and navigates back to login reactively

**Today**
- Live countdown to next lesson / break (1 Hz ticker)
- 5-day (Mon–Fri) week-at-a-glance grid under the countdown card
- Weekend placeholder copy

**Academic (Scores)**
- `POST /Stu/Exam/GetScoreData` with term picker (`ExposedDropdownMenuBox`)
- iOS-parity score normalization (`"-"` → `"0"` for raw, `""` for IB)
- Per-subject card with T1–T5 chip rows

**Settings**
- Real student ID + username from login profile
- Term picker wired to `YearRepository` (persists via `SecureCredentialStore`)
- Logout

**CAS**
- 3-tab scaffold: **My Clubs** · **Browse** · **Evaluation**
- `GetMyGroupList` / `GetGroupList` (paginated infinite scroll over all ~100 clubs) / `JoinGroup`
- Club detail screen with intro card (group no / teacher / full description) + Records and Reflections sub-tabs
- `GetRecordList` / `GetReflectionList` read flow
- **Full write flow**: add / edit / delete records (with ≥80-word reflection validation, C/A/S duration inputs, date field) and reflections (title / summary / content / LO chip selector) via `SaveRecord` / `DeleteRecord` / `SaveReflection` / `DeleteReflection`
- `GetEvaluateData` semester breakdown (Rec / Ref / Talk / Final + per-club hours)

**Year options**
- Scrapes `<select id="YearId">` from `/Stu/Timetable/Index` with Jsoup
- Persisted `currentYearId` shared across Timetable / Scores / CAS evaluation

**Design system**
- Material 3 theme with light/dark palettes aligned to the iOS app
- Shared `AppSpace` / `AppRadius` tokens

**Tests**
- `TodayViewModelTest`, `ApiEnvelopeTest`, `TimetableMapperTest`, `TimetableWeekMapperTest`, `TsimsClientValidatorTest`
- `YearOptionParserTest` — Jsoup scrape
- `ScoreRepositoryTest` — iOS-parity score normalization
- `CasDtoTest` — envelope decoding, `Theme`/`Title` fallback, dash-duration normalization, LO int → enum, paged envelope, plain-list envelope, evaluation mapping

**CI**
- GitHub Actions: `assembleDebug` + `test`

## Not yet implemented

- **Notices / Exam schedule / Attendance** endpoints
- **CAS leader approval** (`GetGroupLeader`, confirm records) and progress ring (`GetRecordProgerss`)
- **Room caching** / offline-first repositories
- **Pull-to-refresh** on Today / Academic / CAS
- **Rich HTML rendering** for reflection content (currently plain-text stripped)
- **Keep-alive WorkManager** job to extend TSIMS session
- **Biometric gate** on scores
- **Glance widget**, deep links, App Links
- **Proper HTTPS transport** — the app currently talks plain HTTP to the TSIMS host; must be wrapped before any store submission

## Project layout

```
app/src/main/java/com/computerization/outspire/
├── MainActivity.kt / OutspireApp.kt
├── designsystem/                 # Theme / Tokens / Color / Type
├── navigation/                   # Root scaffold + bottom nav + live auth gating
├── data/
│   ├── local/                    # SecureCredentialStore
│   ├── remote/                   # Ktor client, services, DTOs, cookie jar
│   │   ├── AuthService.kt        # login / logout / withAuthRetry
│   │   ├── TimetableService.kt
│   │   ├── ScoreService.kt
│   │   ├── YearService.kt        # Jsoup year-options scrape
│   │   ├── CasService.kt         # all CAS endpoints
│   │   └── dto/
│   ├── repository/               # AuthRepository, TimetableRepository, ScoreRepository, YearRepository, CasRepository
│   └── model/                    # Domain models
└── feature/
    ├── login/
    ├── today/                    # countdown + week grid
    ├── academic/                 # scores
    ├── cas/                      # 3-tab CAS + club detail + editor dialogs
    └── settings/
```

## Gradle Wrapper

The `gradle-wrapper.jar` binary is **not committed**. After cloning, either run:

```bash
gradle wrapper --gradle-version 8.9
```

or open the project in Android Studio — it will regenerate the wrapper automatically.

## License

TBD — intended to follow the iOS repo's license.
