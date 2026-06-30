package nl.rhaydus.softcover.core.preferences.di

import org.koin.core.module.Module

/**
 * Platform-provided Koin bindings the shared `preferencesModule` pulls in via `includes(...)`. Each
 * target supplies the two pieces that cannot be shared: the `AppSettingsDataStore` file location and
 * the `SecureApiKeyStorage` implementation.
 */
expect val platformPreferencesModule: Module
