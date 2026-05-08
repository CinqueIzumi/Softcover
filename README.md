# Softcover

A native Android client for [Hardcover.app](https://hardcover.app/) — track what you read, what you've read, and what's next.

Built with Kotlin and Jetpack Compose. Designed for Android 8.0+.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.01-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/min%20SDK-26-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Project Documentation](#project-documentation)
- [Disclaimer](#disclaimer)

---

## Features

### Library
- Browse books organized by status — **Want to Read**, **Currently Reading**, **Read**, **Did Not Finish**, **Owned**.
- Add and remove books from your library.
- Mark editions as owned.

### Reading
- See currently reading books and audiobooks at a glance.
- Track progress by page number or percentage for book editions.
- Track listening progress in `HH:MM:SS` or percentage for audiobook editions.
- Mark books as read in one tap.

### Explore
- Browse trending books on Hardcover.
- Pick up where you left off with a **Continue Series** shelf for series you're partway through.
- Search the Hardcover catalogue by title and add or remove books directly from results.
- Review and manage your search history.

### Book Details
- View title, author, description, ratings, release date, and page count or audiobook duration.
- Change reading status — Want to Read, Currently Reading, Read, Did Not Finish.
- Update reading or listening progress.
- Set a reading deadline with a required pages-per-day or minutes-per-day pace.
- Switch between book and audiobook editions.

### Profile
- View your profile and reading statistics.
- Log out.

### Settings
- Toggle between a floating and docked bottom navigation bar.
- Choose your preferred date format.

---

## Tech Stack

#### Core
| Technology | Version | Purpose |
|---|---|---|
| [Kotlin](https://kotlinlang.org/) | 2.2.21 | Primary language |
| [Jetpack Compose](https://developer.android.com/jetpack/compose) | BOM 2026.01.01 | Declarative UI framework |
| [Material 3](https://m3.material.io/) | 1.5.0-alpha13 | Design system and theming |
| [Gradle (KTS)](https://gradle.org/) | 8.13.1 | Build system with version catalog |
| [KSP](https://github.com/google/ksp) | 2.2.21-2.0.4 | Kotlin Symbol Processing for Room |

#### Data & Networking
| Technology | Version | Purpose |
|---|---|---|
| [Apollo](https://www.apollographql.com/docs/kotlin) | 4.3.3 | GraphQL API client |
| [Room](https://developer.android.com/jetpack/androidx/releases/room) | 2.7.2 | Local database for caching books |
| [DataStore](https://developer.android.com/jetpack/androidx/releases/datastore) | 1.2.0 | Key-value storage for settings and search history |
| [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization) | 1.9.0 | JSON serialization |

#### App Architecture
| Technology | Version | Purpose |
|---|---|---|
| [Voyager](https://voyager.adriel.cafe/) | 1.1.0-beta02 | Screen navigation and state holder models |
| [Koin](https://insert-koin.io/docs/setup/koin/) | 3.5.3 | Dependency injection |
| [Coil](https://github.com/coil-kt/coil) | 2.7.0 | Image loading |
| [Timber](https://github.com/JakeWharton/timber) | 5.0.1 | Logging |

#### Testing
| Technology | Version | Purpose |
|---|---|---|
| [JUnit 5](https://junit.org/junit5/) | 5.11.4 | Unit test runner (Jupiter API, engine, params) |
| [MockK](https://mockk.io/) | 1.13.17 | Mocking library for Kotlin |
| [Kotest](https://kotest.io/) | 5.9.1 | Fluent assertions (`shouldBe`, etc.) |
| [kotlinx-coroutines-test](https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test) | 1.10.2 | Coroutine test utilities (`runTest`) |
| [Turbine](https://github.com/cashapp/turbine) | 1.2.0 | Testing library for Kotlin `Flow` |

#### Platform
- **Minimum SDK:** 26 (Android 8.0)
- **Target SDK:** 36
- **Java compatibility:** 11

---

## Getting Started

### Prerequisites
- Android Studio (latest stable)
- JDK 11+
- A Hardcover.app account and API token

### Build & Run
```bash
./gradlew assembleDebug          # Build a debug APK
./gradlew assembleRelease        # Build a release APK
./gradlew test                   # Run unit tests
./gradlew :app:test              # Run unit tests for the app module
./gradlew connectedAndroidTest   # Run instrumented tests (device/emulator required)
./gradlew lint                   # Run Android Lint
```

---

## Project Documentation

| Document | Purpose |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Clean Architecture layout and the TOAD state-management framework |
| [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) | Color roles, editorial typography, layout primitives, components, and patterns |
| [CODE_STYLE_GUIDE.md](CODE_STYLE_GUIDE.md) | Naming, layout, and whitespace conventions |
| [CLAUDE.md](CLAUDE.md) | Guidance for Claude Code when working in this repo |

---

## Disclaimer

This project is an independent, open-source application and is **not affiliated with, endorsed by, or sponsored by** [Hardcover.app](https://hardcover.app/).
