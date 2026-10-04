package nl.rhaydus.softcover.core.component.control

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import nl.rhaydus.softcover.core.designsystem.presentation.theme.dynamicColorSchemeOrNull
import nl.rhaydus.softcover.core.designsystem.presentation.theme.softcoverColorScheme

/** The triangle below a bottom-left-to-top-right seam — the half of the System tile painted dark. */
private val DIAGONAL_LOWER_HALF: Shape = GenericShape { size, _ ->
    moveTo(
        0f,
        size.height,
    )
    lineTo(
        size.width,
        0f,
    )
    lineTo(
        size.width,
        size.height,
    )
    close()
}

/**
 * One choice in the Appearance screen's theme picker: a miniature of the app's own page — accent bar,
 * headline, two lines of body, one card — painted in the scheme [model]'s painting would actually
 * give, over its label beneath. Selection, press feedback, and semantics come from [PreviewTileFrame].
 *
 * The tile shows the theme the reader is *not* in, so it can't read colours off `MaterialTheme`; it
 * resolves both sides of the pair itself through [softcoverColorScheme] — in [model]'s chosen spine
 * colour, so the tiles wear the same palette the app does — and through [dynamicColorSchemeOrNull]
 * when [model]'s dynamic colour flag is on, so what the tile promises is what picking it delivers.
 * [ThemeTilePainting.SPLIT] is drawn as one tile split on the diagonal — light above the seam, dark
 * below — rather than as a third flat swatch, because "whichever your device is" has no single colour.
 *
 * It lives in the component library rather than in `feature/settings` because the onboarding theme
 * step is a second consumer in waiting, and because the schemes it paints come from tokens the library
 * already depends on. Reach for this anatomy for any "pick a whole-app look" choice; never render a
 * theme as a bare colour swatch or a radio row.
 */
@Composable
fun <K> ThemePreviewTile(
    model: ThemePreviewTileUiModel<K>,
    onEvent: (PreviewTileEvent<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lightScheme = dynamicColorSchemeOrNull(
        useDynamicColor = model.dynamicColor,
        darkTheme = false,
    )
        ?: softcoverColorScheme(
            darkTheme = false,
            palette = model.palette,
        )

    val darkScheme = dynamicColorSchemeOrNull(
        useDynamicColor = model.dynamicColor,
        darkTheme = true,
    )
        ?: softcoverColorScheme(
            darkTheme = true,
            palette = model.palette,
        )

    // The split tile paints the light scheme underneath and clips the dark one over its lower half,
    // so its base is the light one; the other two paintings are simply themselves.
    val baseScheme = if (model.painting == ThemeTilePainting.DARK) darkScheme else lightScheme

    PreviewTileFrame(
        label = model.label,
        selected = model.selected,
        onClick = { onEvent(PreviewTileEvent.Clicked(key = model.key)) },
        modifier = modifier,
    ) {
        PageMiniature(
            scheme = baseScheme,
            accentLed = false,
            modifier = Modifier.matchParentSize(),
        )

        if (model.painting == ThemeTilePainting.SPLIT) {
            PageMiniature(
                scheme = darkScheme,
                accentLed = false,
                modifier = Modifier
                    .matchParentSize()
                    .clip(DIAGONAL_LOWER_HALF),
            )
        }
    }
}
