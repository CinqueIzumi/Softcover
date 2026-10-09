package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.chart.RankedBars
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown

/**
 * "The genres you read most" — one horizontal bar per genre, each filled against its own full-width
 * **100% track**, so a bar's length reads directly as the share of the reader's books carrying that
 * genre. Draws [genreRanking], built from [GenreBreakdown.slices] in the order the domain already
 * ranks them (the domain layer keeps the top five and drops the rest).
 *
 * Deliberately **not** the stacked proportion bar this section used before 3.1.1. A stacked bar
 * asserts a partition of a whole, and these shares are not parts of a whole: genres overlap, so five
 * of them can each be 60% and the set can total well past 100. Giving every genre its own common
 * track is the shape that survives that — each bar is read against 100%, never against its
 * neighbours, so there is no whole to be a remainder of and no "gap" left to correct.
 *
 * Rank is carried by the stepping tones of [genreRanking] (monochrome `primary`, alpha carries
 * emphasis) rather than by bar length alone, so two genres within a point of each other still read as
 * first and second.
 */
@Composable
internal fun GenreRankingSection(
    genres: GenreBreakdown,
    genreRanking: RankedBarsUiModel,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (genres.slices.isEmpty() && isLoading.not()) {
            SoftcoverText(
                text = "Once a few books settle onto your shelves, this fills in.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            RankedBars(model = genreRanking)

            Spacer(modifier = Modifier.height(Spacing.md))

            SoftcoverText(
                text = genreScopeCaption(taggedBookCount = genres.taggedBookCount),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                isLoading = isLoading,
                standIn = "Your five most-read genres, each as a share of your books, counted over your " +
                    "finished books that carry genre tags. A book tagged with several counts " +
                    "toward each, so these overlap.",
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
