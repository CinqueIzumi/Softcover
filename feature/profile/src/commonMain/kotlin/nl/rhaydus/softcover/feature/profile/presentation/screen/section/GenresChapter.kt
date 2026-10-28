package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chart.RankedBars
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * The genre ranking under its [header]. [showMore] and [showLess] are null while there is nothing to reveal
 * or fold. "Show more" grows the rows in place; "Show less" first scrolls the Atlas to the top of the
 * chapter and only then folds them, so the rows never shrink above the reader's viewport.
 */
@Composable
internal fun GenresChapter(
    header: SectionHeaderUiModel,
    ranking: RankedBarsUiModel,
    showMore: PillButtonUiModel?,
    showLess: PillButtonUiModel?,
    onShowMore: () -> Unit,
    onShowLess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHaptics.current
    val scope = rememberCoroutineScope()
    val playMotion = playDecorativeMotion()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    Column(modifier = modifier) {
        ScopedSectionHeader(
            header = header,
            modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
        )

        if (ranking.rows.isNotEmpty() || ranking.isLoading) {
            Spacer(modifier = Modifier.height(Spacing.xl))

            RankedBars(model = ranking)
        }

        val hasButtons = showMore != null || showLess != null

        Row(modifier = if (hasButtons) Modifier.padding(top = Spacing.md) else Modifier) {
            RevealButton(
                model = showMore,
                playMotion = playMotion,
                modifier = Modifier.padding(end = Spacing.xs),
                onClick = {
                    haptics.tickle()

                    onShowMore()
                },
            )

            RevealButton(
                model = showLess,
                playMotion = playMotion,
                onClick = {
                    haptics.tickle()

                    scope.launch {
                        bringIntoViewRequester.bringIntoView()

                        onShowLess()
                    }
                },
            )
        }
    }
}

/**
 * A button that fades and slides its width in and out as [model] appears and disappears, holding the last
 * model so the exit still has a button to draw.
 */
@Composable
private fun RevealButton(
    model: PillButtonUiModel?,
    playMotion: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var lastModel by remember { mutableStateOf(model) }

    if (model != null) lastModel = model

    val shown = lastModel ?: return

    if (playMotion.not()) {
        if (model != null) {
            PillButton(
                model = shown,
                onEvent = { onClick() },
                modifier = modifier,
            )
        }

        return
    }

    AnimatedVisibility(
        visible = model != null,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally(),
    ) {
        PillButton(
            model = shown,
            onEvent = { onClick() },
            modifier = modifier,
        )
    }
}
