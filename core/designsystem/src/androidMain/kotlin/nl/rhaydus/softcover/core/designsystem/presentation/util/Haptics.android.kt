package nl.rhaydus.softcover.core.designsystem.presentation.util

import android.annotation.SuppressLint
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

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

@Composable
actual fun rememberHaptics(): Haptics {
    val view = LocalView.current

    return remember(view) { ViewHaptics(view) }
}
