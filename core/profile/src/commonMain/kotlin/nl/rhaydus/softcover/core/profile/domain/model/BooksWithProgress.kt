package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book

internal data class BooksWithProgress(
    val books: List<Book>,
    val updates: List<ProgressUpdate>,
)
