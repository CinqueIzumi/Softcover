package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice

private val GENRE_BAR_ALPHAS = listOf(1f, 0.82f, 0.64f, 0.48f, 0.34f)
private val GENRE_BAR_HEIGHT = 10.dp
private const val GENRE_TRACK_ALPHA = 0.14f
private const val GENRE_BAR_MIN_FRACTION = 0.015f
private const val GENRE_SKELETON_ROWS = 3

/**
 * "The genres you read most" — one horizontal bar per genre, each filled against its own full-width
 * **100% track**, so a bar's length reads directly as the share of the reader's books carrying that
 * genre. Reads the [GenreSlice]s in the order [GenreBreakdown.slices] already ranks them (the domain
 * layer keeps the top five and drops the rest).
 *
 * Deliberately **not** the stacked proportion bar this section used before 3.1.1. A stacked bar
 * asserts a partition of a whole, and these shares are not parts of a whole: genres overlap, so five
 * of them can each be 60% and the set can total well past 100. Giving every genre its own common
 * track is the shape that survives that — each bar is read against 100%, never against its
 * neighbours, so there is no whole to be a remainder of and no "gap" left to correct.
 *
 * Rank is carried by the stepping alphas this section has always used (§2.1: monochrome `primary`,
 * shape carries the data, alpha carries emphasis) rather than by bar length alone, so two genres
 * within a point of each other still read as first and second.
 */
@Composable
internal fun GenreRankingSection(
    genres: GenreBreakdown,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionIntro(
            eyebrow = "What you reach for",
            headline = "The genres you read most",
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (genres.slices.isEmpty() && isLoading.not()) {
            Text(
                text = "Once a few books settle onto your shelves, this fills in.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            GenreRankedBars(
                slices = genres.slices,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = genreScopeCaption(taggedBookCount = genres.taggedBookCount),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.shimmer(isLoading = isLoading),
            )
        }
    }
}

/**
 * Names the denominator in words, because the bars alone can't: a share of *books* is only meaningful
 * once the reader knows which books were counted. Untagged books are outside the scope entirely, so
 * the sentence says so rather than leaving "your books" to be read as the whole shelf.
 *
 * Falls back to the scope-free wording when the count is 0 — that is either a genuinely empty library
 * or a profile cached by a build older than 3.1.1 (the stored count defaults to 0 and fills in on the
 * session's refresh), and naming "0 books" beneath five populated bars would be worse than saying
 * nothing.
 */
private fun genreScopeCaption(taggedBookCount: Int): String {
    val scope = when (taggedBookCount) {
        0 -> "counted over your finished books that carry genre tags"
        1 -> "counted over the single finished book of yours that carries genre tags"
        else -> "counted over the ${formatGroupedNumber(taggedBookCount)} finished books of yours that carry genre tags"
    }

    return "Your five most-read genres, each as a share of your books — $scope. " +
        "A book tagged with several counts toward each, so these overlap and don't total 100%."
}

@Composable
private fun GenreRankedBars(
    slices: List<GenreSlice>,
    isLoading: Boolean,
) {
    val rows = slices.take(GENRE_BAR_ALPHAS.size)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Keeps the section from collapsing to its intro alone through the first load: with no slices
        // yet there is nothing to draw, and an empty Column would leave a bare headline that then
        // jumps as the data lands. The skeleton reserves the tracks only, not the name/percentage rows
        // above them, so the section still grows on arrival - this softens the jump rather than
        // removing it, which is the same trade the legend-less loading state made before it.
        if (rows.isEmpty()) {
            repeat(GENRE_SKELETON_ROWS) { index ->
                GenreBarTrack(
                    fraction = 0f,
                    alpha = GENRE_BAR_ALPHAS[index],
                    isLoading = isLoading,
                )
            }
        } else {
            rows.forEachIndexed { index, slice ->
                GenreRankedBar(
                    slice = slice,
                    alpha = GENRE_BAR_ALPHAS[index],
                    isLoading = isLoading,
                )
            }
        }
    }
}

@Composable
private fun GenreRankedBar(
    slice: GenreSlice,
    alpha: Float,
    isLoading: Boolean,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = slice.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${(slice.fraction * PERCENTAGE_MULTIPLIER).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        GenreBarTrack(
            fraction = slice.fraction.toFloat(),
            alpha = alpha,
            isLoading = isLoading,
        )
    }
}

// The fill is floored so a genre that rounds to a visible percentage still draws something, and
// capped at the full track so a share can never overrun it. Unlike the stacked bar this replaced,
// the floor costs nothing from a neighbour — each track is its own 100%.
@Composable
private fun GenreBarTrack(
    fraction: Float,
    alpha: Float,
    isLoading: Boolean,
) {
    val shape = RoundedCornerShape(3.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(GENRE_BAR_HEIGHT)
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = GENRE_TRACK_ALPHA))
            .shimmer(shape = shape, isLoading = isLoading),
    ) {
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(
                        fraction.coerceIn(
                            GENRE_BAR_MIN_FRACTION,
                            1f,
                        ),
                    )
                    .fillMaxHeight()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha)),
            )
        }
    }
}
