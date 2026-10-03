package nl.rhaydus.softcover.core.component.callout

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.EditorialTypography

@Immutable
internal data class BannerStyle(
    val container: Color,
    val content: Color,
    val dismissInk: Color,
    val textStyle: TextStyle,
    val textAlign: TextAlign,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
)

internal fun bannerStyle(
    tone: BannerTone,
    variant: BannerVariant,
    scheme: ColorScheme,
    typography: Typography,
    editorialTypography: EditorialTypography,
): BannerStyle {
    val (container, content) = when (tone) {
        BannerTone.Warning -> scheme.errorContainer to scheme.onErrorContainer
        BannerTone.Highlight -> scheme.primaryContainer to scheme.onPrimaryContainer
    }

    val (textStyle, textAlign) = when (variant) {
        BannerVariant.Status -> typography.bodySmall to TextAlign.Center
        BannerVariant.Editorial -> editorialTypography.bodySmall to TextAlign.Start
    }

    return BannerStyle(
        container = container,
        content = content,
        dismissInk = content.copy(alpha = 0.8f),
        textStyle = textStyle,
        textAlign = textAlign,
        horizontalPadding = 16.dp,
        verticalPadding = 10.dp,
    )
}
