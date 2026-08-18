package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.softcover.core.designsystem.presentation.theme.dynamicColorSchemeOrNull
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.softcoverColorScheme
import nl.rhaydus.softcover.core.domain.model.ThemeMode

private val TILE_SHAPE: Shape = RoundedCornerShape(14.dp)
private const val TILE_ASPECT_RATIO = 0.78f

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
 * mode's name beneath.
 *
 * The tile shows the theme the reader is *not* in, so it can't read colours off `MaterialTheme`; it
 * resolves both sides of the pair itself through [softcoverColorScheme], and through
 * [dynamicColorSchemeOrNull] when [dynamicColor] is on, so what the tile promises is what picking it
 * delivers. [ThemeMode.SYSTEM] is drawn as one tile split on the diagonal — light above the seam, dark
 * below — rather than as a third flat swatch, because "whichever your device is" has no single colour.
 *
 * Selection is carried by a `primary` ring plus the `primary` label (no check badge — the ring already
 * says it once), and by radio-button semantics for the reader who can't see either.
 */
@Composable
fun ThemePreviewTile(
    mode: ThemeMode,
    selected: Boolean,
    dynamicColor: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lightScheme = dynamicColorSchemeOrNull(
        useDynamicColor = dynamicColor,
        darkTheme = false,
    )
        ?: softcoverColorScheme(darkTheme = false)

    val darkScheme = dynamicColorSchemeOrNull(
        useDynamicColor = dynamicColor,
        darkTheme = true,
    )
        ?: softcoverColorScheme(darkTheme = true)

    // The System tile paints the light scheme underneath and clips the dark one over its lower half,
    // so its base is the light one; the other two modes are simply themselves.
    val baseScheme = if (mode == ThemeMode.DARK) darkScheme else lightScheme

    val interactionSource = remember { MutableInteractionSource() }

    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        modifier = modifier
            .pointerHandCursor()
            .pressScale(interactionSource)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(TILE_ASPECT_RATIO)
                .clip(TILE_SHAPE)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = borderColor,
                    shape = TILE_SHAPE,
                ),
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

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = mode.label,
            style = MaterialTheme.editorialTypography.titleSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
        )
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

/** One rounded rule standing in for a run of type — or, in `primary`, for the accent bar. */
@Composable
private fun MiniBar(
    widthFraction: Float,
    height: Dp,
    color: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(2.dp))
            .background(color),
    )
}
