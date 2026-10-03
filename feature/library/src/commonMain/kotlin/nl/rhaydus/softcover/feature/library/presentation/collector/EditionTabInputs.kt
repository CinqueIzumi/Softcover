package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.BookEdition

/** Everything one custom-list tab's cards are derived from; equal inputs mean equal cards. */
internal data class EditionTabInputs(
    val editions: List<BookEdition>,
    val covers: List<CoverUiModel?>,
    val press: BookCardDecorations.Press,
    val coverOnly: Boolean,
)
