# Outspire for Android

A fresh native Android implementation of [Computerization/Outspire](https://github.com/Computerization/Outspire). It uses the Swift project as its behavioural and visual specification and does not reuse the abandoned Android repository.

## Current milestone

This project provides a working application foundation:

- Kotlin and Jetpack Compose UI with Material 3
- feature-based MVVM structure
- four primary destinations: Today, Class, Activities, and Explore
- live one-second timetable progress and the original nine-period schedule
- weekday preview, self-study filling, subject colours, and quick links
- explicit demo mode with representative timetable, grades, and CAS records
- cookie-based TSIMS login and session verification
- TSIMS academic-year discovery and timetable decoding for both response formats used by the Swift app
- account, grades, CAS summary, dark theme, deep-link declaration, and unit tests

Live score and CAS endpoints are deliberately isolated behind `OutspireRepository`; their Compose screens already consume the domain models. They can be connected without changing the UI.

## Requirements

- Android Studio compatible with Android Gradle Plugin 8.13
- JDK 17
- Android SDK 36
- Gradle 8.13
- Android 8.0 or later on the device (`minSdk 26`)

The Gradle wrapper binary is omitted from this generated source archive. Generate it once with an installed Gradle:

```bash
gradle wrapper --gradle-version 8.13
./gradlew test assembleDebug
```

Alternatively, open the project in Android Studio and run the `app` configuration after generating or selecting Gradle 8.13.

## Configure TSIMS

Copy `local.properties.example` to `local.properties`, add the Android SDK path, and set the server URL:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
tsims.baseUrl=https://your-secure-tsims-relay.example
```

Release builds reject cleartext HTTP. Debug builds permit it only for migration testing because the original Swift app currently points to an HTTP server. Credentials are kept in memory and are cleared when the process ends; secure credential persistence is intentionally deferred until biometric and Android Keystore requirements are defined.

If `tsims.baseUrl` is empty, live login returns a configuration message and the user can select **Continue with demo data**.

## Structure

```text
app/src/main/java/dev/outspire/android/
├── MainActivity.kt
├── data/
│   ├── model/          Domain models and schedule logic
│   ├── remote/         Isolated TSIMS HTTP client
│   └── repository/     Repository contract, live adapter, demo data
├── designsystem/       Theme, tokens, cards, status components
├── feature/
│   ├── account/
│   ├── academic/
│   ├── activities/
│   ├── explore/
│   └── today/
└── navigation/         Root tabs and detail routes
```

## Migration order

1. Harden authentication with Android Keystore-backed storage and reauthentication.
2. Connect scores, including term selection and biometric protection.
3. Connect CAS club browsing, activity records, reflections, and editing.
4. Add school arrangements, dining menus, caching, and offline fallback.
5. Add notifications, App Links, a Glance widget, and background session maintenance.

## Source reference

The implementation was mapped from the original repository at commit `ed9dbfa3c481863844695e10ab9fded07ae5b0ea`, including `RootTabView`, `ClassPeriodsManager`, `ClassInfoParser`, `AuthServiceV2`, `TSIMSClientV2`, and `TimetableServiceV2`.
