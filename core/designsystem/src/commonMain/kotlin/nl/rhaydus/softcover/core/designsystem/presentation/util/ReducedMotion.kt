package nl.rhaydus.softcover.core.designsystem.presentation.util

import androidx.compose.runtime.Composable

/**
 * Whether decorative motion should play on the current device.
 *
 * Android reads `Settings.Global.ANIMATOR_DURATION_SCALE` (a scale of `0f` means animations are
 * disabled system-wide); iOS reads `UIAccessibility.isReduceMotionEnabled`. Either way, decorative
 * motion is skipped when the user has asked the system to reduce motion.
 *
 * Functional motion (a state change the user just triggered — e.g. a list-item swipe or a navigation
 * push) still plays; this gate covers decorative motion only.
 */
@Composable
expect fun playDecorativeMotion(): Boolean
