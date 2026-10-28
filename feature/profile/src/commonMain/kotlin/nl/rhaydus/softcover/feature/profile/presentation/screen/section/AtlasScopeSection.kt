package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.layout.WindowWidthClass
import nl.rhaydus.designsystem.layout.rememberWindowSizeClass
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.StatsScope

/**
 * The scope every scoped section below answers for: a single-select chip row and the quiet line that
 * says which dates it covers. The row waits for the reading history, so year chips never pop in and the
 * selection never visibly hops. Compact and medium scroll the row sideways; expanded wraps it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AtlasScopeSection(
    scopeChips: ChipSet<StatsScope>,
    isHistoryLoaded: Boolean,
    scopeLine: String,
    onScopeSelected: (StatsScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHaptics.current
    val wrap = rememberWindowSizeClass().widthClass == WindowWidthClass.EXPANDED

    val onChipEvent: (ChipEvent) -> Unit = { event ->
        if (event is ChipEvent.Clicked) {
            scopeChips[event.key]?.let { selected ->
                haptics.select()
                onScopeSelected(selected)
            }
        }
    }

    val chipContent: @Composable () -> Unit = {
        scopeChips.chips.forEach { chip ->
            Chip(
                model = chip,
                onEvent = onChipEvent,
            )
        }
    }

    val playMotion = playDecorativeMotion()

    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = isHistoryLoaded,
            enter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None,
            exit = if (playMotion) shrinkVertically() + fadeOut() else ExitTransition.None,
        ) {
            Column {
                if (wrap) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        chipContent()
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(state = rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        chipContent()
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.sm))
            }
        }

        FadeThroughText(
            text = scopeLine,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
