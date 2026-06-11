package nl.rhaydus.softcover.feature.app_update.di

import org.koin.core.module.Module

/**
 * Platform-provided Koin binding the shared [appUpdateModule] pulls in via `includes(...)`. The
 * pieces that cannot be shared are the [AppUpdateDataSource][nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSource]
 * implementation — Android wraps Google Play's `AppUpdateManager`, iOS binds a no-op that always
 * reports [AppUpdateState.Idle][nl.rhaydus.softcover.core.domain.model.AppUpdateState] — and, on
 * Android, the [AppUpdateFlowLauncher][nl.rhaydus.softcover.feature.app_update.domain.launcher.AppUpdateFlowLauncher].
 *
 * The Android actual also binds the Play `AppUpdateManager` — debuggable builds get a
 * `FakeAppUpdateManager`, signed release builds get the real manager — chosen at runtime via
 * `ApplicationInfo.FLAG_DEBUGGABLE`, so no build-type source sets are needed.
 */
expect val platformAppUpdateModule: Module
