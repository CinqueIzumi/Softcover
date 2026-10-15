package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalSchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
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
 * The tile paints in the brightness the reader is *in* ([LocalSchemeBrightness]) rather than
 * resolving both, since that half of the choice belongs to the theme picker above it. Dim has no
 * per-palette look, so in Dim the tile paints the palette's dark half. It always paints its own
 * palette, even while dynamic colour is overriding the scheme app-wide — five identical
 * wallpaper-coloured tiles would say nothing; the section's gloss line carries that state instead,
 * and picking a palette takes the page back.
 *
 * A model with [ColorPalettePreviewTileUiModel.enabled] false renders muted and inert, for a picker
 * whose choice is set aside by the theme above it.
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
        brightness = if (LocalSchemeBrightness.current.isDark) {
            SchemeBrightness.DARK
        } else {
            SchemeBrightness.LIGHT
        },
        palette = model.palette,
    )

    PreviewTileFrame(
        label = model.palette.label,
        selected = model.selected,
        enabled = model.enabled,
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
