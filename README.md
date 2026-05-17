# Browser — Android WebView Browser

A minimal Android browser built with Kotlin, following Clean Architecture and MVVM patterns.

## Architecture

```
data/          → Room database, DAOs, repository implementations
domain/        → Models, repository interfaces, use cases
presentation/  → ViewModel (StateFlow), UI (Activity + XML)
di/            → Hilt modules
util/          → URL parsing, UserAgent management
```

## Stack

- **Language:** Kotlin
- **UI:** XML layouts + ViewBinding
- **Architecture:** MVVM + Clean Architecture
- **DI:** Hilt
- **Persistence:** Room
- **State:** StateFlow + coroutines
- **CI:** GitHub Actions (lint → test → build)

## Features (v1)

- Search bar (URL or Google search)
- Back / Forward / Refresh / Share / Bookmark
- Desktop mode toggle
- 3-tier bottom sheet (peek → mid → full)
- History recording
- Back press: sheet → WebView history → exit

## Build

```bash
./gradlew assembleDebug
```

## Test

```bash
# Unit tests
./gradlew testDebugUnitTest

# Lint
./gradlew lintDebug

# ktlint
./gradlew ktlintCheck
```

## CI/CD

GitHub Actions runs on every push:
1. **Lint** — ktlint + Android lint
2. **Test** — unit tests
3. **Build** — debug APK (all branches), release APK (main only)

### Required Secrets (for release builds)

| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded release keystore |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

## License

MIT
