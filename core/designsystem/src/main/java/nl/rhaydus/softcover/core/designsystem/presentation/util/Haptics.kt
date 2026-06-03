package nl.rhaydus.softcover.core.designsystem.presentation.util

import android.annotation.SuppressLint
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView

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

private class ViewHaptics(private val view: View) : Haptics {
    override fun commit() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }

        view.performHapticFeedback(constant)
    }

    override fun reject() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.CONTEXT_CLICK
        }

        view.performHapticFeedback(constant)
    }

    override fun select() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.SEGMENT_TICK
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }

        view.performHapticFeedback(constant)
    }

    override fun threshold() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }

        view.performHapticFeedback(constant)
    }

    override fun tickle() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }

        view.performHapticFeedback(constant)
    }

    // SDK_INT >= R guards the API 30 constant; the int is safely inlined on older APIs.
    @SuppressLint("InlinedApi")
    override fun lift() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.DRAG_START
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }

        view.performHapticFeedback(constant)
    }

    override fun drop() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.GESTURE_END
        } else {
            HapticFeedbackConstants.CONTEXT_CLICK
        }

        view.performHapticFeedback(constant)
    }

    override fun milestone() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }

        view.performHapticFeedback(constant)

        view.postDelayed(
            { view.performHapticFeedback(constant) },
            MILESTONE_GAP_MS,
        )
    }

    private companion object {
        const val MILESTONE_GAP_MS = 120L
    }
}

private object NoOpHaptics : Haptics {
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

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current

    return remember(view) { ViewHaptics(view) }
}
