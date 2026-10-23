package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardRail
import nl.rhaydus.softcover.core.component.bookcard.BookCardRailEvent
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.LoadMoreSimilarBooksAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * The "If you liked this" rail. It shows stand-ins while the first page loads and collapses when there is
 * nothing to show. [listState] is hoisted above the lens switch so the rail keeps its scroll position.
 */
@Composable
internal fun SimilarBooksSection(
    state: BookDetailUiState,
    listState: LazyListState,
    runAction: (BookDetailAction) -> Unit,
) {
    if (state.similarBooks.set.isEmpty) return

    val onCardEvent = rememberPrefetchingBookCardEventHandler(
        set = state.similarBooks.set,
    ) { event, payload ->
        if (event is BookCardEvent.Clicked) runAction(payload)
    }

    val onRailEvent = remember(onCardEvent, runAction) {
        { event: BookCardRailEvent ->
            when (event) {
                is BookCardRailEvent.Card -> onCardEvent(event.event)
                BookCardRailEvent.NearEnd -> runAction(LoadMoreSimilarBooksAction())
            }
        }
    }

    Spacer(modifier = Modifier.height(Spacing.section))

    Column {
        SectionHeader(
            model = state.similarBooksSectionHeader,
            modifier = Modifier.padding(horizontal = Spacing.gutter),
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        BookCardRail(
            model = state.similarBooks,
            onEvent = onRailEvent,
            listState = listState,
        )
    }
}
