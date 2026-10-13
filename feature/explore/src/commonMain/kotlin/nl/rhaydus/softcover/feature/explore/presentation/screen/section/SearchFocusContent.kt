package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.transition.ExitingItems
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnMoodChipClickedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveAllSearchQueriesClickedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveSearchQueryClickedAction

/**
 * The search-focus overlay content (explore-3a §4 "Search focus state"): the recent-query history as
 * full rows (clock leading icon, trailing per-row remove) followed by "Try a mood" chips. Replaces the
 * feed while the field is focused and empty — no editorial recommendations here, those live on the feed.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SearchFocusContent(
    queries: ImmutableList<String>,
    moodChips: ChipSet<MoodTag>,
    runAction: (ExploreAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val effectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val spatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<IntSize>()

    Column(modifier = modifier.padding(horizontal = Spacing.gutter)) {
        AnimatedVisibility(
            visible = queries.isNotEmpty(),
            enter = fadeIn(animationSpec = effectsSpec) + expandVertically(animationSpec = spatialSpec),
            exit = fadeOut(animationSpec = effectsSpec) + shrinkVertically(animationSpec = spatialSpec),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SoftcoverText(
                        text = "RECENT",
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    RhaydusButton(
                        label = "Clear all",
                        onClick = { runAction(OnRemoveAllSearchQueriesClickedAction()) },
                        style = ButtonStyle.TEXT,
                    )
                }

                Column(modifier = Modifier.padding(top = Spacing.xs)) {
                    ExitingItems(
                        items = queries,
                        keyOf = { it },
                        exit = fadeOut(animationSpec = effectsSpec) + shrinkVertically(animationSpec = spatialSpec),
                    ) { query, _ ->
                        SearchFocusRecentRow(
                            query = query,
                            onClick = {
                                runAction(
                                    OnQueryChangeAction(
                                        newQuery = query,
                                        searchDelay = 0.seconds,
                                    ),
                                )
                            },
                            onRemoveClick = { runAction(OnRemoveSearchQueryClickedAction(query = query)) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }

        if (moodChips.isEmpty.not()) {
            SoftcoverText(
                text = "TRY A MOOD",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ChipFlow(
                set = moodChips,
                onEvent = { event, _ ->
                    if (event is ChipEvent.Clicked) {
                        runAction(OnMoodChipClickedAction(key = event.key))
                    }
                },
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
    }
}

@Composable
private fun SearchFocusRecentRow(
    query: String,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .pointerHandCursor()
                    .clickable(onClick = onClick)
                    .padding(vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                val clockIcon = drawableIconResource(
                    icon = SoftcoverIcon.History,
                    contentDescription = "",
                )

                Icon(
                    painter = clockIcon.getIconPainter(),
                    contentDescription = clockIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )

                SoftcoverText(
                    text = query,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .pointerHandCursor()
                    .clickable(
                        role = Role.Button,
                        onClick = onRemoveClick,
                    ),
                contentAlignment = Alignment.CenterEnd,
            ) {
                val closeIcon = drawableIconResource(
                    icon = SoftcoverIcon.Close,
                    contentDescription = "Remove query",
                )

                Icon(
                    painter = closeIcon.getIconPainter(),
                    contentDescription = closeIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp),
                )
            }
        }

        Divider()
    }
}

