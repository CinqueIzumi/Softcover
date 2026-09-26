package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.component.control.ThemePreviewTile
import nl.rhaydus.softcover.core.component.control.ThemePreviewTileEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.action.OnThemeModeSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

/**
 * The theme picker: one [ThemePreviewTile] per
 * [ThemeMode][nl.rhaydus.softcover.core.domain.model.ThemeMode], each painting the app's own page in the
 * scheme it would give — in the reader's chosen spine colour, so the two pickers agree — so the choice is
 * made by looking rather than by reading three words. It leads the Appearance body — including on desktop,
 * ahead of "Display scale" — because it is the one control that changes every surface in the app.
 */
@Composable
internal fun ThemeSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Theme",
            headline = "The coat it wears",
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PREVIEW_TILE_GAP),
        ) {
            state.themeChoices.forEach { choice ->
                ThemePreviewTile(
                    model = choice.tile,
                    onEvent = { event ->
                        when (event) {
                            ThemePreviewTileEvent.Clicked ->
                                runAction(OnThemeModeSelectedAction(mode = choice.mode))
                        }
                    },
                    modifier = Modifier
                        .weight(
                            weight = 1f,
                            fill = false,
                        )
                        .widthIn(max = PREVIEW_TILE_MAX_WIDTH),
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "System follows your device's own light and dark setting.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
