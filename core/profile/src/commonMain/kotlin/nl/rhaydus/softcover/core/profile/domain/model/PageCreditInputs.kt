package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book

/** Everything pages are credited from: [updates] in the order they were logged, and the Read and Did not finish books. */
internal data class PageCreditInputs(
    val updates: List<ProgressUpdate>,
    val readBooks: List<Book>,
    val didNotFinishBooks: List<Book>,
)
