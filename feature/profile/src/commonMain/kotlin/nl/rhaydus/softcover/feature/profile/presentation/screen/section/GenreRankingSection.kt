package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    hasGenres: Boolean,
    genreScopeCaption: String,
    genreRanking: RankedBarsUiModel,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        if (hasGenres.not() && isLoading.not()) {
            SoftcoverText(
                text = "Finish a few books and your top genres show up here.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            RankedBars(model = genreRanking)

            Spacer(modifier = Modifier.height(Spacing.md))

            SoftcoverText(
                text = genreScopeCaption,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                isLoading = isLoading,
                standIn = "Your top five genres, out of your finished books with genre tags. Books can have more " +
                    "than one genre, so these add up to more than 100%.",
            )
        }
    }
}
