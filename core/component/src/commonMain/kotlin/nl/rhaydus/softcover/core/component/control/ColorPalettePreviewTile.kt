package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalDarkTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.softcoverColorScheme

/**
 * One choice in the Appearance screen's spine-colour picker: the same page miniature the theme tiles
 * show, painted in [model]'s palette entire — its paper as well as its ink. Selection, press feedback,
 * and semantics come from [PreviewTileFrame], so a palette tile and a theme tile are the same object.
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
 *
 * Never render a palette as a bare colour swatch, a paint chip, or a radio row.
 */
@Composable
fun <K> ColorPalettePreviewTile(
    model: ColorPalettePreviewTileUiModel<K>,
    onEvent: (PreviewTileEvent<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = softcoverColorScheme(
        darkTheme = LocalDarkTheme.current,
        palette = model.palette,
    )

    PreviewTileFrame(
        label = model.palette.label,
        selected = model.selected,
        onClick = { onEvent(PreviewTileEvent.Clicked(key = model.key)) },
        modifier = modifier,
    ) {
        PageMiniature(
            scheme = scheme,
            accentLed = true,
            modifier = Modifier.matchParentSize(),
        )
    }
}
