package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTile
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTileEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.action.OnColorPaletteSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDynamicColorToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.util.supportsDynamicColor

/**
 * The gap between the spine-colour picker's two rows — wider than [PREVIEW_TILE_GAP], since the
 * vertical run has each tile's own label in it and the next row has to clear that, not just the tile.
 */
private val PREVIEW_TILE_ROW_GAP = 18.dp

/** The spine-colour picker wraps after three tiles, so its five sit as a 3 + 2 grid. */
private const val PALETTE_TILES_PER_ROW = 3

/**
 * The spine-colour picker — the featured personalisation of the Appearance body: one
 * [ColorPalettePreviewTile] per [ColorPalette][nl.rhaydus.softcover.core.domain.model.ColorPalette], each
 * painting the same page miniature in that palette's own paper and ink, so five curated looks are compared
 * by looking at them side by side. The gloss line beneath names what the *selected* palette is made of
 * rather than repeating a fixed sentence — it is the only place the picker says anything in words.
 *
 * Dynamic colour is this section's tail rather than a Display switch, because it is the alternative
 * *scheme source*: while it is on it takes the whole scheme from the wallpaper and the palette steps
 * aside, so the gloss says so and picking any tile takes the page back
 * ([OnColorPaletteSelectedAction]). It stays gated on [supportsDynamicColor], so on iOS and desktop
 * the section is the tiles alone.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SpineColourSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Spine colour",
            headline = "The paper and the ink",
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Every tile is measured to the same width — a third of the row, capped — rather than
        // weighted: with weights the wrapped second row's two tiles would each claim half the width
        // and end up visibly larger than the three above them.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tileWidth = minOf(
                PREVIEW_TILE_MAX_WIDTH,
                (maxWidth - PREVIEW_TILE_GAP * (PALETTE_TILES_PER_ROW - 1)) / PALETTE_TILES_PER_ROW,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PREVIEW_TILE_GAP),
                verticalArrangement = Arrangement.spacedBy(PREVIEW_TILE_ROW_GAP),
                maxItemsInEachRow = PALETTE_TILES_PER_ROW,
            ) {
                state.paletteChoices.forEach { choice ->
                    ColorPalettePreviewTile(
                        model = choice.tile,
                        onEvent = { event ->
                            when (event) {
                                ColorPalettePreviewTileEvent.Clicked ->
                                    runAction(OnColorPaletteSelectedAction(palette = choice.colorPalette))
                            }
                        },
                        modifier = Modifier.width(tileWidth),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (state.useDynamicColorChecked) {
                "Dynamic colour is painting the app — pick a spine colour to take it back."
            } else {
                state.paletteGloss
            },
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        if (supportsDynamicColor()) {
            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsToggleRow(
                label = "Dynamic colour",
                gloss = "Take the whole scheme from your wallpaper instead of a spine colour.",
                checked = state.useDynamicColorChecked,
                onCheckedChange = { runAction(OnDynamicColorToggledAction(newValue = it)) },
            )
        }
    }
}
