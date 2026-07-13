---
name: project-migration-test-pattern
description: How to write and wire Room migration tests in androidHostTest using BundledSQLiteDriver on JVM
metadata:
  type: project
---

Room migration tests live in `core/database/src/androidHostTest/kotlin/.../migration/`, one file per migration.

## Runtime dependency wiring

`BundledSQLiteDriver` ships Android native libs in the KMP artifact but **JVM native libs** in `androidx.sqlite:sqlite-bundled-jvm`. The `androidHostTest` JVM runner needs the latter:
- Added alias `androidx-sqlite-bundled-jvm` to `gradle/libs.versions.toml`.
- Added `runtimeOnly(libs.androidx.sqlite.bundled.jvm)` to `androidHostTest.dependencies` in `core/database/build.gradle.kts`.

Without this, every test fails with `NoClassDefFoundError: BundledSQLiteDriver` / `ExceptionInInitializerError`.

## Test pattern

```kotlin
private val migration = SoftcoverDatabase.ALL_MIGRATIONS
    .single { it.startVersion == X && it.endVersion == Y }

@BeforeEach fun setUp() { connection = BundledSQLiteDriver().open(":memory:"); buildVxSchema(connection) }
@AfterEach  fun tearDown() { connection.close() }
```

Helper patterns:
- `PRAGMA table_info(table)` col index 1 → column name (for schema shape assertions).
- `SELECT name FROM sqlite_master WHERE type='table'/'index' AND name=?` → existence checks.
- `stmt.prepare(...)` / `stmt.step()` / accessors, always in `try/finally { stmt.close() }` (`.use{}` may not be available).
- Call `stmt.bindText(1, value)` before stepping prepared statements with parameters.

**Why:** `BundledSQLiteDriver().open(":memory:")` gives a real in-memory SQLite connection per test — no emulator, no Room, no schema JSONs needed. Synchronous `migration.migrate(connection)` calls make `runTest` unnecessary.

## Correct Gradle task name for this KMP module's androidHostTest

`:core:database:testDebugUnitTest` does **not** exist (that's the plain-Android-module naming). The real task is `:core:database:testAndroidHostTest`. When filtering, `--tests "*ClassName*"` works fine against it. Confirmed working:
`./gradlew :core:database:testAndroidHostTest --tests "*Migration44To45Test*" --tests "*DismissedContinueSeriesDaoTest*"`
