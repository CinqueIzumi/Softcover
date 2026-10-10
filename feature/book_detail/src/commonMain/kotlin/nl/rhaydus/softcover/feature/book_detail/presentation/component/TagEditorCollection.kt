package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagEditorChipGroup

@Composable
internal fun TagEditorCollection(
    groups: List<UserTagEditorChipGroup>,
    emptyState: EmptyStateUiModel,
    newlyAddedTagKey: String?,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (groups.isEmpty()) {
            EmptyState(
                model = emptyState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.x4l),
            )
        } else {
            groups.forEachIndexed { index, group ->
                TagGroupSection(
                    group = group,
                    newlyAddedTagKey = newlyAddedTagKey,
                    onEvent = onEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (index == 0) Spacing.xxs else Spacing.xl),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagGroupSection(
    group: UserTagEditorChipGroup,
    newlyAddedTagKey: String?,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftcoverText(
                text = group.label.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.width(Spacing.xs))

            Divider(
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(Spacing.xs))

            SoftcoverText(
                text = group.chips.size.toString(),
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            group.chips.forEach { chip ->
                key(chip.key) {
                    EditorTagChip(
                        chip = chip,
                        isNewlyAdded = chip.key == newlyAddedTagKey,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

/**
 * Plays a brief fade + rise on the first composition where [isNewlyAdded] is true — i.e. the render
 * pass that just inserted this chip — and never re-triggers on later recompositions of the same chip
 * (its `key(chip.key)` slot in [TagGroupSection] keeps the `remember` below alive).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditorTagChip(
    chip: ChipUiModel,
    isNewlyAdded: Boolean,
    onEvent: (ChipEvent) -> Unit,
) {
    val density = LocalDensity.current

    val visibleState = remember {
        MutableTransitionState(initialState = isNewlyAdded.not())
    }

    LaunchedEffect(Unit) {
        visibleState.targetState = true
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
            slideInVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) {
                with(density) { 3.dp.roundToPx() }
            },
        exit = ExitTransition.None,
    ) {
        Chip(
            model = chip,
            onEvent = onEvent,
        )
    }
}
