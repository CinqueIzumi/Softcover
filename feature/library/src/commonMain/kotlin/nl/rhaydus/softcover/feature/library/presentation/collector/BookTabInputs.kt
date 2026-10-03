package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.domain.model.Book

/** Everything one book tab's cards are derived from; equal inputs mean equal cards. */
internal data class BookTabInputs(
    val books: List<Book>,
    val covers: List<CoverUiModel?>,
    val marks: List<SelectionMarkUiModel?>,
    val overlays: List<CoverOverlayUiModel?>,
    val press: BookCardDecorations.Press,
    val coverOnly: Boolean,
)
