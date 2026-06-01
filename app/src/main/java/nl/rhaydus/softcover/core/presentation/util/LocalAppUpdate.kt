package nl.rhaydus.softcover.core.presentation.util

import androidx.compose.runtime.compositionLocalOf
import nl.rhaydus.softcover.feature.app_update.domain.model.AppUpdateState

val LocalAppUpdateState = compositionLocalOf<AppUpdateState> { AppUpdateState.Idle }

val LocalStartAppUpdate = compositionLocalOf<() -> Unit> { {} }
