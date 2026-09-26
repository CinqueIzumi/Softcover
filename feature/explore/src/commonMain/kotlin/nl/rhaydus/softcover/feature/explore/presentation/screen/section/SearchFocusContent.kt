package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnMoodChipClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveAllSearchQueriesClickedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveSearchQueryClickedAction

/**
 * The search-focus overlay content (explore-3a §4 "Search focus state"): the recent-query history as
 * full rows (clock leading icon, trailing per-row remove) followed by "Try a mood" chips. Replaces the
 * feed while the field is focused and empty — no editorial recommendations here, those live on the feed.
 */
@Composable
internal fun SearchFocusContent(
    queries: List<String>,
    moods: List<MoodTag>,
    runAction: (ExploreAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        if (queries.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
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

            Column(modifier = Modifier.padding(top = 8.dp)) {
                queries.forEach { query ->
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

            Spacer(modifier = Modifier.height(28.dp))
        }

        if (moods.isNotEmpty()) {
            Text(
                text = "TRY A MOOD",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRowMoodChips(
                moods = moods,
                onMoodClick = { mood -> runAction(OnMoodChipClickAction(mood = mood)) },
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
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
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

            Text(
                text = query,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            val closeIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Remove query",
            )

            Icon(
                painter = closeIcon.getIconPainter(),
                contentDescription = closeIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(15.dp)
                    .noRippleClickable(onClick = onRemoveClick),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowMoodChips(
    moods: List<MoodTag>,
    onMoodClick: (MoodTag) -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        moods.forEach { mood ->
            RecentSearchChip(
                query = mood.label.toTitleCaseWords(),
                onClick = { onMoodClick(mood) },
            )
        }
    }
}
