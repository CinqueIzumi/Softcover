---
name: project-kmp-patterns
description: KMP migration patterns observed in core:preferences — expect/actual DI, platform source sets, value class annotation
metadata:
  type: project
---

Observed during `:core:preferences` KMP migration (branch `refactor/kmp`):

- **expect/actual for platform Koin modules**: `expect val platformPreferencesModule: Module` in commonMain, actuals in androidMain and iosMain. This is the established pattern for platform-split DI.
- **Platform security abstraction**: `SecureApiKeyStorage` interface in commonMain; `AndroidSecureApiKeyStorage` (Keystore+AES/GCM+file) in androidMain; `IosSecureApiKeyStorage` (Keychain via Security.framework) in iosMain.
- **@JvmInline in commonMain is wrong**: `AppSettingsDataStore` uses `@JvmInline value class` in commonMain. The annotation `kotlin.jvm.JvmInline` is JVM-specific and should not appear in KMP commonMain source sets — just use `value class` without the annotation.
- **KMP convention plugin** (`softcover.kmp.library` / `KmpLibraryConventionPlugin`) provides `koin-core` in commonMain automatically; modules add `koin.android` in androidMain for `androidContext()`.
- **androidHostTest** source set is used for JVM-only test tools (JUnit5, MockK) that cannot run in commonTest.

**Why:** So future reviewers know which KMP patterns are established vs. which are new/wrong.
**How to apply:** Flag `@JvmInline` on commonMain value classes. Confirm expect/actual Koin modules for other KMP migrations.
