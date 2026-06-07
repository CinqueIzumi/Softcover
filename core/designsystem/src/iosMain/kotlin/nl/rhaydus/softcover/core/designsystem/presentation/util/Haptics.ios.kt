package nl.rhaydus.softcover.core.designsystem.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

/**
 * Maps the shared [Haptics] vocabulary onto UIKit's feedback generators: notification feedback for
 * commit/reject/milestone, selection feedback for the soft ticks, and impact feedback for the
 * physical boundary/reorder cues.
 */
private class IosHaptics : Haptics {
    override fun commit() = notify(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)

    override fun reject() = notify(UINotificationFeedbackType.UINotificationFeedbackTypeError)

    override fun select() = UISelectionFeedbackGenerator().selectionChanged()

    override fun threshold() = impact(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)

    override fun tickle() = UISelectionFeedbackGenerator().selectionChanged()

    override fun lift() = impact(UIImpactFeedbackStyle.UIImpactFeedbackStyleRigid)

    override fun drop() = impact(UIImpactFeedbackStyle.UIImpactFeedbackStyleSoft)

    override fun milestone() {
        notify(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)

        dispatch_after(
            dispatch_time(
                DISPATCH_TIME_NOW,
                MILESTONE_GAP_NANOS,
            ),
            dispatch_get_main_queue(),
        ) {
            notify(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
        }
    }

    private fun impact(style: UIImpactFeedbackStyle) {
        UIImpactFeedbackGenerator(style).impactOccurred()
    }

    private fun notify(type: UINotificationFeedbackType) {
        UINotificationFeedbackGenerator().notificationOccurred(type)
    }

    private companion object {
        const val MILESTONE_GAP_NANOS = 120_000_000L
    }
}

@Composable
actual fun rememberHaptics(): Haptics = remember { IosHaptics() }
