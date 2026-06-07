package nl.rhaydus.softcover.core.designsystem.presentation.util

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
actual fun playDecorativeMotion(): Boolean = UIAccessibilityIsReduceMotionEnabled().not()
