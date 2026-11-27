package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.BookStatus

internal fun BookStatus?.isStarted(): Boolean = when (this) {
    BookStatus.Reading, BookStatus.Paused, BookStatus.Read, BookStatus.DidNotFinish -> true
    BookStatus.WantToRead, BookStatus.None, null -> false
}
