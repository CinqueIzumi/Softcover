package nl.rhaydus.softcover.core.designsystem.presentation.component

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
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalDarkTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.designsystem.presentation.theme.softcoverColorScheme

/**
 * One choice in the Appearance screen's spine-colour picker: the same page miniature the theme tiles
 * show, painted in [palette] entire — its paper as well as its ink. Selection, press feedback, and
 * semantics come from [PreviewTileFrame], so a palette tile and a theme tile are the same object.
 *
 * The miniature leans harder on the accent than [ThemePreviewTile]'s does — its card is a
 * `primary`-**filled** hero (the app's real hero-stat treatment) and it carries a `tertiaryContainer`
 * badge beneath — so that one tile carries the whole look: the page tint, the lead colour at full
 * strength, and the second note it is paired with. Two palettes whose papers land close together are
 * still told apart at a glance.
 *
 * The tile paints in the brightness the reader is *in* ([LocalDarkTheme]) rather than resolving both,
 * since that half of the choice belongs to the theme picker above it. It always paints its own
 * palette, even while dynamic colour is overriding the scheme app-wide — five identical
 * wallpaper-coloured tiles would say nothing; the section's gloss line carries that state instead,
 * and picking a palette takes the page back.
 */
@Composable
fun ColorPalettePreviewTile(
    palette: SpinePalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = softcoverColorScheme(
        darkTheme = LocalDarkTheme.current,
        palette = palette,
    )

    PreviewTileFrame(
        label = palette.label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    ) {
        AccentMiniature(
            scheme = scheme,
            modifier = Modifier.matchParentSize(),
        )
    }
}

/**
 * The accent-forward page miniature: the palette's paper under the section accent bar, a headline,
 * one line of body copy, a `primary`-filled hero card, and a `tertiaryContainer` badge — the roles a
 * palette leads with, in the relationship the app actually paints them in.
 */
@Composable
private fun AccentMiniature(
    scheme: ColorScheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(scheme.background)
            .padding(
                horizontal = 10.dp,
                vertical = 12.dp,
            ),
    ) {
        MiniBar(
            widthFraction = 0.18f,
            height = 3.dp,
            color = scheme.primary,
        )

        Spacer(modifier = Modifier.height(9.dp))

        MiniBar(
            widthFraction = 0.82f,
            height = 6.dp,
            color = scheme.onSurface,
        )

        Spacer(modifier = Modifier.height(7.dp))

        MiniBar(
            widthFraction = 1f,
            height = 3.dp,
            color = scheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(scheme.primary)
                .padding(7.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            MiniBar(
                widthFraction = 0.5f,
                height = 4.dp,
                color = scheme.onPrimary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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
