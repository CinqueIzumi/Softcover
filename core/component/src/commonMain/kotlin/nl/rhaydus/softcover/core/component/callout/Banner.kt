package nl.rhaydus.softcover.core.component.callout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A full-width strip of chrome reporting a condition the reader should know about. It declares no
 * shape of its own: at the top of a card, the card's clip gives it matching corners.
 *
 * It owns its own expand/shrink transition, so a caller flips [BannerUiModel.visible] and the banner
 * handles arriving and leaving.
 */
@Composable
fun Banner(
    model: BannerUiModel,
    onEvent: (BannerEvent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val style = bannerStyle(
        tone = model.tone,
        scheme = MaterialTheme.colorScheme,
    )

    AnimatedVisibility(
        visible = model.visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(style.container)
                .padding(
                    horizontal = style.horizontalPadding,
                    vertical = style.verticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (model.icon != null) {
                val leading = drawableIconResource(
                    icon = model.icon,
                    contentDescription = "",
                )

                Icon(
                    painter = leading.getIconPainter(),
                    contentDescription = null,
                    tint = style.content,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(10.dp))
            }

            SoftcoverText(
                text = model.message,
                style = if (style.editorial) {
                    MaterialTheme.editorialTypography.bodySmall
                } else {
                    MaterialTheme.typography.bodySmall
                },
                color = style.content,
                textAlign = style.textAlign,
                modifier = Modifier.weight(1f),
            )

            if (model.dismissible) {
                IconButton(
                    onClick = { onEvent(BannerEvent.DismissClicked) },
                    modifier = Modifier.size(22.dp),
                ) {
                    val close = drawableIconResource(
                        icon = SoftcoverIcon.Close,
                        contentDescription = "Dismiss",
                    )

                    Icon(
                        painter = close.getIconPainter(),
                        contentDescription = close.contentDescription,
                        tint = style.dismissInk,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
