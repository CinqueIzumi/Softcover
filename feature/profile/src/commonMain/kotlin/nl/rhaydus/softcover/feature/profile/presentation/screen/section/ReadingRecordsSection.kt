package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ReadingRecordsUiModel

/**
 * The longest book and the quickest read as two side-by-side tiles. A lone record keeps the left cell.
 * When [ReadingRecordsUiModel.hasRecords] is false the tiles are dropped and the caption says why.
 */
@Composable
internal fun ReadingRecordsSection(
    model: ReadingRecordsUiModel,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AtlasSubhead(
            title = model.title,
            caption = model.caption,
            isLoading = model.isLoading,
        )

        ScopeEmptySwitch(
            isEmpty = model.hasRecords.not() && model.isLoading.not(),
            empty = {},
            filled = {
                Column {
                    Spacer(modifier = Modifier.height(Spacing.md))

                    ScopedRecordCards(
                        cards = model.cards.cards,
                        onEvent = onEvent,
                    )
                }
            },
        )
    }
}

/**
 * The record tiles crossfaded between scopes; an unchanged pair of records does not fade. The lambda
 * reads [modifier] so it is not hoisted into a public `ComposableSingletons` class that would leak
 * [BookCardUiModel] into this module's ABI.
 */
@Composable
private fun ScopedRecordCards(
    cards: ImmutableList<BookCardUiModel>,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScopeCrossfade(
        target = cards,
        contentKey = { scoped -> scoped.map { (it.key as? BookCardKey.Book)?.bookId } },
    ) { scopedCards ->
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            scopedCards.forEach { card ->
                BookCard(
                    model = card,
                    onEvent = onEvent,
                    modifier = Modifier.weight(1f),
                )
            }

            if (scopedCards.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
