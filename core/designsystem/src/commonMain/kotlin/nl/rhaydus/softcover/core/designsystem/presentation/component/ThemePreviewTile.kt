package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.dynamicColorSchemeOrNull
import nl.rhaydus.softcover.core.designsystem.presentation.theme.softcoverColorScheme
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode

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
 * headline, two lines of body, one card — painted in the scheme [mode] would actually give, over the
 * mode's name beneath. Selection, press feedback, and semantics come from [PreviewTileFrame].
 *
 * The tile shows the theme the reader is *not* in, so it can't read colours off `MaterialTheme`; it
 * resolves both sides of the pair itself through [softcoverColorScheme] — in the reader's chosen
 * [colorPalette], so the tiles wear the same spine colour the app does — and through
 * [dynamicColorSchemeOrNull] when [dynamicColor] is on, so what the tile promises is what picking it
 * delivers. [ThemeMode.SYSTEM] is drawn as one tile split on the diagonal — light above the seam,
 * dark below — rather than as a third flat swatch, because "whichever your device is" has no single
 * colour.
 */
@Composable
fun ThemePreviewTile(
    mode: ThemeMode,
    selected: Boolean,
    colorPalette: ColorPalette,
    dynamicColor: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lightScheme = dynamicColorSchemeOrNull(
        useDynamicColor = dynamicColor,
        darkTheme = false,
    )
        ?: softcoverColorScheme(
            darkTheme = false,
            colorPalette = colorPalette,
        )

    val darkScheme = dynamicColorSchemeOrNull(
        useDynamicColor = dynamicColor,
        darkTheme = true,
    )
        ?: softcoverColorScheme(
            darkTheme = true,
            colorPalette = colorPalette,
        )

    // The System tile paints the light scheme underneath and clips the dark one over its lower half,
    // so its base is the light one; the other two modes are simply themselves.
    val baseScheme = if (mode == ThemeMode.DARK) darkScheme else lightScheme

    PreviewTileFrame(
        label = mode.label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    ) {
        ThemeMiniature(
            scheme = baseScheme,
            modifier = Modifier.matchParentSize(),
        )

        if (mode == ThemeMode.SYSTEM) {
            ThemeMiniature(
                scheme = darkScheme,
                modifier = Modifier
                    .matchParentSize()
                    .clip(DIAGONAL_LOWER_HALF),
            )
        }
    }
}

/**
 * The page in miniature, painted from [scheme] rather than from the surrounding theme: the section
 * accent bar, a headline, two lines of body copy, and a card — the app's own editorial rhythm shrunk
 * to a tile, so the reader recognises what they're picking rather than reading a colour swatch.
 */
@Composable
private fun ThemeMiniature(
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

        Spacer(modifier = Modifier.height(4.dp))

        MiniBar(
            widthFraction = 0.62f,
            height = 3.dp,
            color = scheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(scheme.surfaceContainerHigh)
                .padding(7.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            MiniBar(
                widthFraction = 0.55f,
                height = 3.dp,
                color = scheme.primary,
            )
        }
    }
}
