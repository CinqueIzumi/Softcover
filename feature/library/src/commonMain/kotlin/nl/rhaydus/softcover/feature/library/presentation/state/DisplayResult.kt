package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition

/**
 * Output of [DisplayInputs.compute] — the precomputed per-tab display lists and tab stats that
 * [DisplayListsCollector] writes into [LibraryUiState].
 */
internal data class DisplayResult(
    val displayBooksByTab: Map<String, List<Book>>,
    val displayEditionsByTab: Map<String, List<BookEdition>>,
    val tabStatsByTab: Map<String, LibraryTabStats>,
)
