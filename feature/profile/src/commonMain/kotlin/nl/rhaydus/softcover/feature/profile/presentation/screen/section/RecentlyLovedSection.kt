package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLine
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.profile.presentation.collector.RECENTLY_LOVED_CARD_COUNT

private val RECENTLY_LOVED_STAND_IN_CARD = BookCardUiModel(
    key = BookCardKey.Book(bookId = -1),
    variant = BookCardVariant.Column(
        cover = CoverUiModel(
            source = null,
            coverlessTitle = null,
            variant = CoverVariant.ProfileLovedBook,
            isLoading = true,
        ),
        text = BookCardContent(
            title = "The Name of the Wind",
            byline = "By Patrick Rothfuss",
            supportingLines = persistentListOf(
                BookCardLine(parts = persistentListOf(BookCardLinePart.Rating(label = "4.5"))),
                BookCardLine(parts = persistentListOf(BookCardLinePart.Text(text = "May 2026"))),
            ),
        ),
        menuLabel = null,
    ),
)

/**
 * "The last few that landed" — up to three loved reads, each showing the reader's own rating and the
 * month it landed. While [isLoading], or while the loved reads exist but their [cards] have not been
 * mapped yet, [RECENTLY_LOVED_CARD_COUNT] masked cards hold the layout.
 */
@Composable
internal fun RecentlyLovedSection(
    cards: ImmutableList<BookCardUiModel>,
    hasRecentlyLoved: Boolean,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(20.dp))

        if (hasRecentlyLoved.not() && isLoading.not()) {
            SoftcoverText(
                text = "Your next favorite is still ahead of you.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (cards.isEmpty()) {
                    repeat(RECENTLY_LOVED_CARD_COUNT) {
                        BookCard(
                            model = RECENTLY_LOVED_STAND_IN_CARD,
                            onEvent = onEvent,
                            isLoading = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    cards.forEach { card ->
                        BookCard(
                            model = card,
                            onEvent = onEvent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
