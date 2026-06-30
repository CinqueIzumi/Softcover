package nl.rhaydus.softcover.core.designsystem.presentation.debug

import androidx.compose.runtime.Composable

/**
 * Seam for the debug-only routes section surfaced in Settings. Bound per build type in `:app` — the
 * real `DebugRoutesSection` (an `androidMain` debug screen) in debug builds, a no-op in release — so
 * the debug tooling never reaches the release UI. The interface lives in `commonMain` because its
 * sole consumer, `SettingsScreen`, is itself `commonMain`; an `androidMain` declaration would be
 * invisible to it. Build-type-conditional wiring stays in the `:app` application shell, which keeps
 * build types across the multiplatform migration (the KMP Android library plugin produces a single
 * variant).
 */
interface DebugRoutesContent {
    @Composable
    fun Render()
}
