package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.bookCardStaggeredEntry
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book

private val railListStates = ExploreRail.entries.associateWith { LazyListState() }

@Composable
internal fun ExploreCardRail(
    rail: ExploreRail,
    set: BookCardSet<Book>,
    onCardEvent: (BookCardEvent) -> Unit,
) {
    val entry = rememberStaggeredEntryCoordinator(key = rail.staggerKey)

    val cardWidth = when (rail) {
        ExploreRail.UP_NEXT -> 126.dp
        ExploreRail.BECAUSE_YOU_READ, ExploreRail.TRENDING -> 118.dp
    }

    LazyRow(
        state = railListStates.getValue(rail),
        contentPadding = PaddingValues(horizontal = Spacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        itemsIndexed(set.cards, key = { _, card -> card.key.lazyKey }) { index, card ->
            BookCard(
                model = card,
                onEvent = onCardEvent,
                modifier = Modifier
                    .width(cardWidth)
                    .bookCardStaggeredEntry(
                        card = card,
                        coordinator = entry,
                        index = index,
                    ),
            )
        }
    }
}
