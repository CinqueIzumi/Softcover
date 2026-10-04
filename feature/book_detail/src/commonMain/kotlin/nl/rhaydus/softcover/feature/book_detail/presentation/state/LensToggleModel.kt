package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.control.SegmentUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel

/** "Yours" is dimmed and inert until the book has a user copy; the key rides back untouched on selection. */
internal fun lensToggleFor(
    selectedLens: BookDetailLens,
    yoursEnabled: Boolean,
): SegmentedControlUiModel<BookDetailLens> = SegmentedControlUiModel(
    options = persistentListOf(
        SegmentUiModel(
            key = BookDetailLens.THE_BOOK,
            label = "The Book",
        ),
        SegmentUiModel(
            key = BookDetailLens.YOURS,
            label = "Yours",
            enabled = yoursEnabled,
        ),
    ),
    selected = selectedLens,
)
