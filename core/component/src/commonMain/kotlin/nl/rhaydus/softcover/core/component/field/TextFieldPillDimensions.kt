package nl.rhaydus.softcover.core.component.field

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class TextFieldPillDimensions(
    val height: Dp,
    val horizontalPadding: Dp,
    val iconSize: Dp,
    val placeholderAlpha: Float,
    val size: TextFieldPillSize,
) {
    @Composable
    fun container(): Color = when (size) {
        TextFieldPillSize.Compact -> MaterialTheme.colorScheme.surfaceContainer
        TextFieldPillSize.Regular,
        TextFieldPillSize.Large -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    @Composable
    fun textStyle(): TextStyle = when (size) {
        TextFieldPillSize.Compact -> MaterialTheme.typography.bodyMedium
        TextFieldPillSize.Regular -> MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        TextFieldPillSize.Large -> MaterialTheme.typography.bodyLarge
    }
}

internal fun TextFieldPillSize.dimensions(): TextFieldPillDimensions = when (this) {
    TextFieldPillSize.Compact -> TextFieldPillDimensions(
        height = 42.dp,
        horizontalPadding = 14.dp,
        iconSize = 18.dp,
        placeholderAlpha = 1f,
        size = this,
    )

    TextFieldPillSize.Regular -> TextFieldPillDimensions(
        height = 48.dp,
        horizontalPadding = 14.dp,
        iconSize = 20.dp,
        placeholderAlpha = 0.6f,
        size = this,
    )

    TextFieldPillSize.Large -> TextFieldPillDimensions(
        height = 52.dp,
        horizontalPadding = 18.dp,
        iconSize = 20.dp,
        placeholderAlpha = 1f,
        size = this,
    )
}
