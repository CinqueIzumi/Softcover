package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardRail
import nl.rhaydus.softcover.core.component.bookcard.BookCardRailEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardRailUiModel
import nl.rhaydus.softcover.core.domain.model.Book

private val railListStates = ExploreRail.entries.associateWith { LazyListState() }

@Composable
internal fun ExploreCardRail(
    rail: ExploreRail,
    model: BookCardRailUiModel<Book>,
    onCardEvent: (BookCardEvent) -> Unit,
) {
    BookCardRail(
        model = model,
        onEvent = { event ->
            when (event) {
                is BookCardRailEvent.Card -> onCardEvent(event.event)
                // Explore rails are not paged.
                BookCardRailEvent.NearEnd -> Unit
            }
        },
        listState = railListStates.getValue(rail),
    )
}
