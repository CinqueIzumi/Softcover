package nl.rhaydus.softcover.core.designsystem.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

interface Haptics {
    /** Successful commit (mark-as-read, save, confirm). Reserved for celebratory events. */
    fun commit()

    /** Optimistic mutation rolled back, or an action refused. */
    fun reject()

    /** Soft tick on neutral selection: chip toggles, segmented switches, tab carousel changes. */
    fun select()

    /** Single firm tap when a user crosses a meaningful boundary (pull-to-refresh trigger, peek activation, reorder pickup threshold). */
    fun threshold()

    /** Per-integer tick during a slider or picker drag (rating stars, page-number input). */
    fun tickle()

    /** Drag-to-reorder pickup: the item has left the page. */
    fun lift()

    /** Drag-to-reorder settle: the item has returned to the page. */
    fun drop()

    /** Two-pulse celebration above commit: yearly goal hit, 30-day streak, series completed. */
    fun milestone()
}

internal object NoOpHaptics : Haptics {
    override fun commit() = Unit

    override fun reject() = Unit

    override fun select() = Unit

    override fun threshold() = Unit

    override fun tickle() = Unit

    override fun lift() = Unit

    override fun drop() = Unit

    override fun milestone() = Unit
}

val LocalHaptics = staticCompositionLocalOf<Haptics> { NoOpHaptics }

/**
 * The active [Haptics] for the current platform — Android maps to `View.performHapticFeedback`, iOS
 * to `UIFeedbackGenerator`. Provided here (rather than via [LocalHaptics], which defaults to a no-op)
 * so any composable can opt into haptics directly.
 */
@Composable
expect fun rememberHaptics(): Haptics
