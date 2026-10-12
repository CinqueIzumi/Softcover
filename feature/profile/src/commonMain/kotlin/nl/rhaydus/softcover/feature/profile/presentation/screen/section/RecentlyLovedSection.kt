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
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * While [isLoading] with no loved reads yet, [cards] are masked stand-ins that hold the layout.
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

        Spacer(modifier = Modifier.height(Spacing.md))

        if (hasRecentlyLoved.not() && isLoading.not()) {
            SoftcoverText(
                text = "Books you rate highly show up here.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
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
