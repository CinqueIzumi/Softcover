package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExploreCardRail(
    rail: ExploreRail,
    set: BookCardSet<Book>,
    onCardEvent: (BookCardEvent) -> Unit,
) {
    val entry = rememberStaggeredEntryCoordinator(key = rail.staggerKey)

    val cardWidth = when (rail) {
        ExploreRail.UP_NEXT -> 150.dp
        ExploreRail.BECAUSE_YOU_READ, ExploreRail.TRENDING -> 168.dp
    }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        set.cards.forEachIndexed { index, card ->
            key(card.key.lazyKey) {
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
}
