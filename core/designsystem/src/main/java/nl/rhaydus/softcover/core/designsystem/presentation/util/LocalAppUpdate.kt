package nl.rhaydus.softcover.core.designsystem.presentation.util

import androidx.compose.runtime.compositionLocalOf
import nl.rhaydus.softcover.core.domain.model.AppUpdateState

val LocalAppUpdateState = compositionLocalOf<AppUpdateState> { AppUpdateState.Idle }

val LocalStartAppUpdate = compositionLocalOf<() -> Unit> { {} }
