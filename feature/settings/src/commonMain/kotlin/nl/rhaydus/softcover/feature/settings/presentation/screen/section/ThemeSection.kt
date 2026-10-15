package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.control.PreviewTileEvent
import nl.rhaydus.softcover.core.component.control.ThemePreviewTile
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.action.OnThemeModeSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

/** The theme picker wraps after two tiles, so its four sit as a 2 x 2 grid. */
private const val THEME_TILES_PER_ROW = 2

/**
 * The theme picker: one [ThemePreviewTile] per
 * [ThemeMode][nl.rhaydus.softcover.core.domain.model.ThemeMode], each painting the app's own page in the
 * scheme it would give — in the reader's chosen spine colour, so the two pickers agree — so the choice is
 * made by looking rather than by reading four words. It leads the Appearance body — including on desktop,
 * ahead of "Display scale" — because it is the one control that changes every surface in the app.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ThemeSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = state.themeSectionHeader)

        Spacer(modifier = Modifier.height(Spacing.md))

        // Every tile is measured to the same width — half the row, capped — rather than weighted, so the
        // grid keeps the tile size the choices had when they shared one row.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tileWidth = minOf(
                128.dp,
                (maxWidth - Spacing.md * (THEME_TILES_PER_ROW - 1)) / THEME_TILES_PER_ROW,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                maxItemsInEachRow = THEME_TILES_PER_ROW,
            ) {
                state.themeChoices.forEach { tile ->
                    ThemePreviewTile(
                        model = tile,
                        onEvent = { event ->
                            when (event) {
                                is PreviewTileEvent.Clicked ->
                                    runAction(OnThemeModeSelectedAction(mode = event.key))
                            }
                        },
                        modifier = Modifier.width(tileWidth),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        SoftcoverText(
            text = state.themeGloss,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
