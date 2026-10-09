package nl.rhaydus.softcover.core.component.callout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A full-width strip of chrome reporting a condition the reader should know about. It declares no
 * shape of its own: at the top of a card, the card's clip gives it matching corners.
 *
 * It owns its own expand/shrink transition, so a caller flips [BannerUiModel.visible] and the banner
 * handles arriving and leaving.
 *
 * [windowInsets] pads inside the container colour, so the colour fills the inset.
 */
@Composable
fun Banner(
    model: BannerUiModel,
    onEvent: (BannerEvent) -> Unit = {},
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets(0),
) {
    val playMotion = playDecorativeMotion()
    val scheme = MaterialTheme.colorScheme
    val editorial = model.variant == BannerVariant.Editorial
    val container = if (editorial) scheme.primaryContainer else scheme.errorContainer
    val content = if (editorial) scheme.onPrimaryContainer else scheme.onErrorContainer

    AnimatedVisibility(
        visible = model.visible,
        enter = if (playMotion) expandVertically() else EnterTransition.None,
        exit = if (playMotion) shrinkVertically() else ExitTransition.None,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(container)
                .windowInsetsPadding(windowInsets)
                .padding(
                    horizontal = Spacing.md,
                    vertical = Spacing.xs,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (editorial) {
                val leading = drawableIconResource(
                    icon = SoftcoverIcon.Info,
                    contentDescription = "",
                )

                Icon(
                    painter = leading.getIconPainter(),
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(Spacing.sm))
            }

            SoftcoverText(
                text = model.message,
                style = if (editorial) MaterialTheme.editorialTypography.bodySmall else MaterialTheme.typography.bodySmall,
                color = content,
                textAlign = if (editorial) TextAlign.Start else TextAlign.Center,
                modifier = Modifier.weight(1f),
            )

            if (editorial) {
                DismissButton(
                    contentColor = content.copy(alpha = 0.8f),
                    onClick = { onEvent(BannerEvent.DismissClicked) },
                )
            }
        }
    }
}
