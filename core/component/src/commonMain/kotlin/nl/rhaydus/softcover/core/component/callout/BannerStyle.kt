package nl.rhaydus.softcover.core.component.callout

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
internal data class BannerStyle(
    val container: Color,
    val content: Color,
    val dismissInk: Color,
    val textAlign: TextAlign,
    val editorial: Boolean,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
)

internal fun bannerStyle(
    tone: BannerTone,
    scheme: ColorScheme,
): BannerStyle = when (tone) {
    BannerTone.WARNING -> BannerStyle(
        container = scheme.errorContainer,
        content = scheme.onErrorContainer,
        dismissInk = scheme.onErrorContainer.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        editorial = false,
        horizontalPadding = 16.dp,
        verticalPadding = 8.dp,
    )

    BannerTone.HIGHLIGHT -> BannerStyle(
        container = scheme.primaryContainer,
        content = scheme.onPrimaryContainer,
        dismissInk = scheme.onPrimaryContainer.copy(alpha = 0.8f),
        textAlign = TextAlign.Start,
        editorial = true,
        horizontalPadding = 18.dp,
        verticalPadding = 13.dp,
    )
}
