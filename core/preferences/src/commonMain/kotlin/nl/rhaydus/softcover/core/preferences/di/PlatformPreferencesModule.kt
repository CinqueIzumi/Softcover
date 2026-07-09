package nl.rhaydus.softcover.core.preferences.di

import org.koin.core.module.Module

/**
 * Platform-provided Koin bindings the shared `preferencesModule` pulls in via `includes(...)`. Each
 * target supplies the pieces that cannot be shared: the `AppSettingsDataStore` file location, the
 * foundation `nl.rhaydus.platform.SecureStorage` implementation, and — on Android and iOS only — the
 * `LegacySecureApiKeyStorage` reader that carries a pre-foundation key forward.
 */
expect val platformPreferencesModule: Module
