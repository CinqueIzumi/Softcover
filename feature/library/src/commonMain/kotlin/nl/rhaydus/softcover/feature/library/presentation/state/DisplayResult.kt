package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition

/**
 * Output of [DisplayInputs.compute] — the precomputed per-tab display lists and tab stats that
 * [DisplayListsCollector] writes into [LibraryUiState].
 */
internal data class DisplayResult(
    val displayBooksByTab: ImmutableMap<String, ImmutableList<Book>>,
    val displayEditionsByTab: ImmutableMap<String, ImmutableList<BookEdition>>,
    val tabStatsByTab: ImmutableMap<String, LibraryTabStats>,
)
