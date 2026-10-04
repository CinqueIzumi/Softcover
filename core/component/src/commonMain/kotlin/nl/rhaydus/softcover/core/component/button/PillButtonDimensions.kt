package nl.rhaydus.softcover.core.component.button

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Metrics shared by every [PillButtonTone]; only the height varies, by [PillButtonSize]. */
internal object PillButtonDimensions {
    val iconSize = 17.dp
    val iconGap = 6.dp
    val horizontalPadding = 14.dp
    val indicatorSize = 6.dp
    val borderWidth = 1.dp
    const val DISABLED_ALPHA = 0.45f

    fun heightFor(size: PillButtonSize): Dp = when (size) {
        PillButtonSize.Regular -> 38.dp
        PillButtonSize.Large -> 44.dp
    }
}
