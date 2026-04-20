# Softcover
Softcover is a native app for Android based on [Hardcover.app](https://hardcover.app/)

# Tech Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| Language | [Kotlin](https://kotlinlang.org/) | 2.2.21 | Primary language |
| UI | [Jetpack Compose](https://developer.android.com/jetpack/compose) | BOM 2026.01.01 | Declarative UI framework |
| Design | [Material 3](https://m3.material.io/) | 1.5.0-alpha13 | Design system and theming |
| Network | [Apollo](https://www.apollographql.com/docs/kotlin) | 4.3.3 | GraphQL API client |
| Navigation | [Voyager](https://voyager.adriel.cafe/) | 1.1.0-beta02 | Screen navigation and state holder models |
| DI | [Koin](https://insert-koin.io/docs/setup/koin/) | 3.5.3 | Dependency injection |
| Database | [Room](https://developer.android.com/jetpack/androidx/releases/room) | 2.7.2 | Local database for caching books |
| Preferences | [DataStore](https://developer.android.com/jetpack/androidx/releases/datastore) | 1.2.0 | Key-value storage for settings and search history |
| Images | [Coil](https://github.com/coil-kt/coil) | 2.7.0 | Image loading |
| Logging | [Timber](https://github.com/JakeWharton/timber) | 5.0.1 | Logging |
| Serialization | [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization) | 1.9.0 | JSON serialization |
| Build | [Gradle](https://gradle.org/) (KTS) | 8.13.1 | Build system with version catalog |
| Code Gen | [KSP](https://github.com/google/ksp) | 2.2.21-2.0.4 | Kotlin Symbol Processing for Room |
| Testing | [JUnit 5](https://junit.org/junit5/) | 5.11.4 | Unit test runner (Jupiter API, engine, and params) |
| Testing | [MockK](https://mockk.io/) | 1.13.17 | Mocking library for Kotlin |
| Testing | [Kotest](https://kotest.io/) | 5.9.1 | Fluent assertions (`shouldBe`, etc.) |
| Testing | [kotlinx-coroutines-test](https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test) | 1.10.2 | Coroutine test utilities (`runTest`) |
| Testing | [Turbine](https://github.com/cashapp/turbine) | 1.2.0 | Testing library for Kotlin `Flow` |

- Minimum SDK: 26 (Android 8.0)
- Target SDK: 36
- Java compatibility: 11

# Features

### Library
- View books organized by status: Want to Read, Currently Reading, Read, Did Not Finish, and Owned.
- Add and remove books from the library.
- Mark editions as owned.

### Reading
- View currently reading books and audiobooks.
- Track reading progress by page number or percentage for book editions.
- Track listening progress in HH:MM:SS or percentage for audiobook editions.
- Mark books as read.

### Search
- Search for books by name within the data supplied by Hardcover.
- Add or remove books from the library directly from search results.
- View and manage search history.

### Book Details
- View book information including title, author, description, ratings, release date, and either the page count (for book editions) or the audiobook duration.
- Change reading status (Want to Read, Currently Reading, Read, Paused, Did Not Finish).
- Update reading or listening progress.
- Set a reading deadline with a required pages/day or minutes/day pace.
- Switch between book and audiobook editions.

### Profile
- View user profile and reading statistics.
- Log out.

### Settings
- Toggle between floating and docked bottom navigation bar.
- Select date format style.

## Disclaimer
This project is an independent, open-source application and is **not affiliated with, endorsed by, or sponsored by** [Hardcover.app](https://hardcover.app/).