package nl.rhaydus.softcover.core.component.field

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class TextFieldPillDimensions(
    val height: Dp,
    val horizontalPadding: Dp,
    val iconSize: Dp,
    val textStyle: TextStyle,
)

internal fun TextFieldPillSize.dimensions(typography: Typography): TextFieldPillDimensions = when (this) {
    TextFieldPillSize.Compact -> TextFieldPillDimensions(
        height = 42.dp,
        horizontalPadding = 14.dp,
        iconSize = 18.dp,
        textStyle = typography.bodyMedium,
    )

    TextFieldPillSize.Regular -> TextFieldPillDimensions(
        height = 48.dp,
        horizontalPadding = 14.dp,
        iconSize = 20.dp,
        textStyle = typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
    )

    TextFieldPillSize.Large -> TextFieldPillDimensions(
        height = 52.dp,
        horizontalPadding = 18.dp,
        iconSize = 20.dp,
        textStyle = typography.bodyLarge,
    )
}

internal fun TextFieldPillTone.container(colorScheme: ColorScheme): Color = when (this) {
    TextFieldPillTone.Container -> colorScheme.surfaceContainer
    TextFieldPillTone.ContainerHigh -> colorScheme.surfaceContainerHigh
}
