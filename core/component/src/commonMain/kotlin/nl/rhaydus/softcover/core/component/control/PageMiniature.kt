package nl.rhaydus.softcover.core.component.control

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * The page in miniature, painted from [scheme] rather than from the surrounding theme: the section
 * accent bar, a headline, body copy, and a card — the app's own editorial rhythm shrunk to a tile, so
 * the reader recognises what they're picking rather than reading a colour swatch.
 *
 * With [accentLed] the card is `primary`-filled and a `tertiaryContainer` badge follows it, so the
 * palette's lead colour and its second note both show; otherwise the card is a `surfaceContainerHigh`
 * one under two lines of body copy.
 */
@Composable
internal fun PageMiniature(
    scheme: ColorScheme,
    accentLed: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(scheme.background)
            .padding(
                horizontal = Spacing.xs,
                vertical = Spacing.sm,
            ),
    ) {
        MiniBar(
            widthFraction = 0.18f,
            height = 3.dp,
            color = scheme.primary,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        MiniBar(
            widthFraction = 0.82f,
            height = 6.dp,
            color = scheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        MiniBar(
            widthFraction = 1f,
            height = 3.dp,
            color = scheme.onSurfaceVariant,
        )

        if (accentLed.not()) {
            Spacer(modifier = Modifier.height(Spacing.xxs))

            MiniBar(
                widthFraction = 0.62f,
                height = 3.dp,
                color = scheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (accentLed) 34.dp else 30.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (accentLed) scheme.primary else scheme.surfaceContainerHigh)
                .padding(Spacing.xs),
            contentAlignment = Alignment.BottomStart,
        ) {
            MiniBar(
                widthFraction = if (accentLed) 0.5f else 0.55f,
                height = if (accentLed) 4.dp else 3.dp,
                color = if (accentLed) scheme.onPrimary else scheme.primary,
            )
        }

        if (accentLed) {
            Spacer(modifier = Modifier.height(Spacing.xs))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.44f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(scheme.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                MiniBar(
                    widthFraction = 0.55f,
                    height = 3.dp,
                    color = scheme.onTertiaryContainer,
                )
            }
        }
    }
}
