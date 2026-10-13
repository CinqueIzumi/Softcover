package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.feature.app_update.domain.launcher.AppUpdateFlowLauncher
import org.koin.compose.koinInject

// Desktop drives a GitHub-releases-backed self-updater, so the launcher is the real Koin-bound one
// (JvmAppUpdateFlowLauncher, which starts the installer download). Unlike Android's, it needs no
// Compose/Activity scoping, so it resolves straight from the container.
@Composable
internal actual fun rememberAppUpdateFlowLauncher(): AppUpdateFlowLauncher = koinInject<AppUpdateFlowLauncher>()
