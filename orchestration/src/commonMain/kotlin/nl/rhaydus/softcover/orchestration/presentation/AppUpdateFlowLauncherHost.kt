package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.feature.app_update.domain.launcher.AppUpdateFlowLauncher

/**
 * Provides the [AppUpdateFlowLauncher] the root [App] uses to drive an in-app update. The Android
 * implementation must `rememberLauncherForActivityResult` — an Activity-scoped, Compose-only call —
 * before building the launcher via Koin, so the construction cannot live in shared code. Platforms
 * without an in-app update mechanism return a no-op launcher.
 */
@Composable
internal expect fun rememberAppUpdateFlowLauncher(): AppUpdateFlowLauncher
