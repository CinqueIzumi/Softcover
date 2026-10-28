package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.UserTag

/** Everything one custom-list tab's cards are derived from; equal inputs mean equal cards. */
internal data class EditionTabInputs(
    val editions: List<BookEdition>,
    val userTags: List<List<UserTag>>,
    val press: BookCardDecorations.Press,
    val layout: LibraryGridLayout,
)
