package nl.rhaydus.softcover.core.designsystem.presentation.debug

import androidx.compose.runtime.Composable

/**
 * Seam for the debug-only routes section surfaced in Settings. Bound per build type in `:app` — the
 * real [DebugRoutesSection] in debug builds, a no-op in release — so the debug tooling never reaches
 * the release UI. Lives here (rather than relying on a build-type source set) because the KMP Android
 * library plugin produces a single variant; build-type-conditional wiring belongs in the `:app`
 * application shell, which keeps build types across the multiplatform migration.
 */
interface DebugRoutesContent {
    @Composable
    fun Render()
}
