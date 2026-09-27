package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkCosyBackground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkCosyForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkDreadBackground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkDreadEyebrow
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkDreadForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkHeartWrenchBackground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkHeartWrenchForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnMoodChipClickAction
import nl.rhaydus.softcover.feature.explore.presentation.util.toTitleCaseWords

internal const val MOOD_SKELETON_COUNT = 4

private val MOOD_TILE_MIN_HEIGHT = 104.dp

// Mood-tile title exception (design-system.md §2.2): the spec's own literal type role for this
// title ("Mood tile title · italic 600 · 19/22") sits between headlineSmall and titleLarge in the
// editorial scale, so headlineSmall is sized down to it rather than left at its full 24sp.
private val MOOD_TILE_TITLE_FONT_SIZE = 19.sp
private val MOOD_TILE_TITLE_LINE_HEIGHT = 22.sp

/**
 * The 2-column "Browse by mood" grid (explore-3a §4 "Mood grid"). A tap always runs a mood-filtered
 * search — a mood never earns its own feed rail (see [ExploreScreenUiState.searchPhase]'s modelling
 * note). Tile ink cycles by position through the four fixed looks the spec defines; a fifth tag onward
 * repeats the cycle rather than introducing a new colour. Each row is measured at
 * [androidx.compose.foundation.layout.IntrinsicSize.Max] so two tiles whose titles wrap to a
 * different number of lines still land at the same height (explore-3a feedback item 3) — otherwise
 * a two-line mood label next to a one-line one would leave the row visibly uneven.
 */
@Composable
internal fun MoodGrid(
    moods: List<MoodTag>,
    isLoading: Boolean,
    runAction: (ExploreAction) -> Unit,
) {
    if (isLoading.not() && moods.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EditorialSectionHeader(
            eyebrow = "Browse by mood",
            headline = "How do you want to feel?",
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "MoodGrid",
        ) { loading ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (loading) {
                    repeat(MOOD_SKELETON_COUNT / 2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MoodTileSkeleton(modifier = Modifier.weight(1f))

                            MoodTileSkeleton(modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    moods.chunked(2).forEachIndexed { rowIndex, row ->
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            row.forEachIndexed { columnIndex, mood ->
                                MoodTile(
                                    mood = mood,
                                    index = rowIndex * 2 + columnIndex,
                                    onClick = { runAction(OnMoodChipClickAction(mood = mood)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                )
                            }

                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodTile(
    mood: MoodTag,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background: Color
    val foreground: Color
    val eyebrowColor: Color
    val sublineColor: Color

    when (index % 4) {
        0 -> {
            background = MoodInkCosyBackground
            foreground = MoodInkCosyForeground
            eyebrowColor = MoodInkCosyForeground
            sublineColor = MoodInkCosyForeground.copy(alpha = 0.65f)
        }

        1 -> {
            background = MoodInkDreadBackground
            foreground = MoodInkDreadForeground
            eyebrowColor = MoodInkDreadEyebrow
            sublineColor = MoodInkDreadForeground.copy(alpha = 0.6f)
        }

        3 -> {
            background = MoodInkHeartWrenchBackground
            foreground = MoodInkHeartWrenchForeground
            eyebrowColor = MoodInkHeartWrenchForeground
            sublineColor = MoodInkHeartWrenchForeground.copy(alpha = 0.6f)
        }

        else -> {
            background = MaterialTheme.colorScheme.surfaceContainerHigh
            foreground = MaterialTheme.colorScheme.onSurface
            eyebrowColor = MaterialTheme.colorScheme.primary
            sublineColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Column(
        modifier = modifier
            .heightIn(min = MOOD_TILE_MIN_HEIGHT)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "MOOD",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = eyebrowColor,
            )

            val searchIcon = drawableIconResource(
                icon = SoftcoverIcon.Search,
                contentDescription = "",
            )

            Icon(
                painter = searchIcon.getIconPainter(),
                contentDescription = searchIcon.contentDescription,
                tint = foreground.copy(alpha = 0.75f),
                modifier = Modifier.size(16.dp),
            )
        }

        Column {
            // Mood-tile title exception (design-system.md §2.2): headlineSmall sized down to the
            // spec's own literal 19/22 mood-tile-title role rather than the full headlineMedium the
            // grid shipped with, which read oversize against a ~140dp half-grid tile and forced a
            // mid-word break on a long single-word mood ("Adventurous", "Mysterious") — the API
            // returns these lowercase and unbroken by spaces, so there is no word boundary to wrap
            // on until the glyphs themselves shrink to fit. `Hyphens.None` + `LineBreak.Heading`
            // mirror the same mid-word-break guard `CoverlessTitleCover` uses.
            Text(
                text = mood.label.toTitleCaseWords(),
                style = MaterialTheme.editorialTypography.headlineSmall.copy(
                    fontSize = MOOD_TILE_TITLE_FONT_SIZE,
                    lineHeight = MOOD_TILE_TITLE_LINE_HEIGHT,
                    hyphens = Hyphens.None,
                    lineBreak = LineBreak.Heading,
                ),
                color = foreground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = "${formatGroupedNumber(mood.bookCount)} books",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = sublineColor,
            )
        }
    }
}

@Composable
private fun MoodTileSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = MOOD_TILE_MIN_HEIGHT)
            .clip(RoundedCornerShape(16.dp))
            .shimmer(isLoading = true),
    )
}
