package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.toad.LocalVariables

internal data class BookDetailLocalVariables(
    val editionsLoadedForBookId: Int? = null,
    val bookMutationJobs: Map<Int, Job> = emptyMap(),
    val editionMutationJobs: Map<Int, Job> = emptyMap(),
    val tagSaveJob: Job? = null,
    val tagVocabularySynced: Boolean = false,

    /** The book id the similar-books list belongs to; set once its first page starts, so it loads once per source. */
    val similarBooksSourceId: Int? = null,

    /** The next rank offset to request; advanced by each page's `nextOffset`. */
    val similarBooksOffset: Int = 0,

    /** The running similar-books page load; the guard against overlapping loads. */
    val similarBooksLoadJob: Job? = null,

    /** Every similar book loaded so far, in rank order; `UiState` carries their ids only. */
    val similarBooks: List<Book> = emptyList(),
) : LocalVariables
