package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.softcover.feature.settings.presentation.action.OnFloatingBarToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnReadingStreakToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnShelfSwipeToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

/**
 * The remaining appearance switches, collapsed into one flat, hairline-divided stack (no boxed cards,
 * no per-row accent bar or icon). Each applicable row is built as a [ToggleRowSpec] first so the
 * divider placement (between rows, never before the first) doesn't need to special-case the platform
 * gating. Dynamic colour is deliberately *not* here — it belongs to [ThemeSection], whose tiles it
 * recolours.
 */
@Composable
internal fun DisplaySection(
    state: SettingsScreenUiState,
    showBottomBarToggle: Boolean,
    showShelfSwipeToggle: Boolean,
    runAction: (SettingsAction) -> Unit,
) {
    val rows = buildList {
        if (showBottomBarToggle) {
            add(
                ToggleRowSpec(
                    label = "Floating bottom bar",
                    gloss = "Lift the nav off the edge, with rounded corners.",
                    checked = state.useFloatingBarChecked,
                    onCheckedChange = { runAction(OnFloatingBarToggledAction(newValue = it)) },
                ),
            )
        }

        if (showShelfSwipeToggle) {
            add(
                ToggleRowSpec(
                    label = "Swipe between shelves",
                    gloss = "Flick left or right in your Library to move to the next shelf.",
                    checked = state.shelfSwipeEnabledChecked,
                    onCheckedChange = { runAction(OnShelfSwipeToggledAction(newValue = it)) },
                ),
            )
        }

        add(
            ToggleRowSpec(
                label = "Reading streak",
                gloss = "Count the days you read in a row, shown on your profile.",
                checked = state.readingStreakEnabledChecked,
                onCheckedChange = { runAction(OnReadingStreakToggledAction(newValue = it)) },
            ),
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Display",
            headline = "The look",
        )

        Spacer(modifier = Modifier.height(20.dp))

        rows.forEachIndexed { index, row ->
            if (index > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            SettingsToggleRow(
                label = row.label,
                gloss = row.gloss,
                checked = row.checked,
                onCheckedChange = row.onCheckedChange,
            )
        }
    }
}

private data class ToggleRowSpec(
    val label: String,
    val gloss: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
)
