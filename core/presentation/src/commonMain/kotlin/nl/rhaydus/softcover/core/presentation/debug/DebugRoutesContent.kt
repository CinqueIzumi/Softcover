package nl.rhaydus.softcover.core.presentation.debug

import androidx.compose.runtime.Composable

/**
 * Seam for the debug-only routes section surfaced in Settings. Bound per build type in `:app` — the
 * real `DebugRoutesSection` (`app/src/debug/`, Android-only) in debug builds, a no-op in release — so
 * the debug tooling never reaches the release UI.
 *
 * `:app` is the only module with build types (the KMP Android library plugin produces a single
 * variant), which is why both the screens and their bindings live there: an implementation in a
 * library module would ship in release regardless of the binding. The interface itself is
 * `commonMain` here because its consumer, `SettingsScreen`, is `commonMain`, and because
 * `:core:presentation` is where the app's cross-tier presentation seams live — it is not a token, so
 * it does not belong in `:core:designsystem`, and it is not a component, so it does not belong in
 * `:core:component`.
 */
interface DebugRoutesContent {
    @Composable
    fun Render()
}
