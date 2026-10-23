package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardRail
import nl.rhaydus.softcover.core.component.bookcard.BookCardRailEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardRailUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnAddRecommendedToWantToReadAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnRecommendedMenuOpenAction

@Composable
internal fun RecommendedRail(
    header: SectionHeaderUiModel,
    rail: BookCardRailUiModel<Book>,
    onBookClick: (Book) -> Unit,
    runAction: (LibraryAction) -> Unit,
) {
    val onCardEvent = rememberPrefetchingBookCardEventHandler(set = rail.set) { event, book ->
        when (event) {
            is BookCardEvent.Clicked -> onBookClick(book)

            is BookCardEvent.TrailingClicked -> runAction(OnAddRecommendedToWantToReadAction(book = book))

            is BookCardEvent.MenuClicked -> runAction(OnRecommendedMenuOpenAction(bookId = book.id))

            else -> Unit
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.padding(top = Spacing.xl),
    ) {
        SectionHeader(
            model = header,
            modifier = Modifier.padding(horizontal = rail.horizontalInset),
        )

        BookCardRail(
            model = rail,
            onEvent = { event ->
                when (event) {
                    is BookCardRailEvent.Card -> onCardEvent(event.event)

                    // The recommended rail is not paged.
                    BookCardRailEvent.NearEnd -> Unit
                }
            },
            listState = rememberLazyListState(),
        )
    }
}
