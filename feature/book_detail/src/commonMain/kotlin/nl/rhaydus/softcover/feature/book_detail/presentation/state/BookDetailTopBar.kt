package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.topbar.TopBarSurface
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.domain.model.Book

internal fun topBarFor(book: Book?): TopBarUiModel =
    TopBarUiModel(
        title = book?.title,
        surface = TopBarSurface.Opaque,
    )

/** This state holding [book], with [BookDetailUiState.topBar] rebuilt to carry its title. */
internal fun BookDetailUiState.withBook(book: Book?): BookDetailUiState =
    copy(
        book = book,
        topBar = topBarFor(book = book),
    )
