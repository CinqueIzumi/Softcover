package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardSelection
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.UserTag

/** Everything one book tab's cards are derived from; equal inputs mean equal cards. */
internal data class BookTabInputs(
    val books: List<Book>,
    val deadlines: List<BookDeadline?>,
    val userTags: List<List<UserTag>>,
    val dateStyle: DateStyle,
    val selections: List<BookCardSelection?>,
    val press: BookCardDecorations.Press,
    val layout: LibraryGridLayout,
)
