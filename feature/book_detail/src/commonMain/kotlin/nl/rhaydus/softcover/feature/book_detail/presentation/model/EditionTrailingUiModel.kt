package nl.rhaydus.softcover.feature.book_detail.presentation.model

import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel

/**
 * What an edition card in the edition selector trails, keyed by [nl.rhaydus.softcover.core.domain.model.BookEdition.id]
 * in [nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState.editionTrailingMarks]. A missing
 * key means the card trails nothing.
 */
internal sealed interface EditionTrailingUiModel {
    data class Selected(val mark: SelectionMarkUiModel) : EditionTrailingUiModel

    data class Tracking(val badge: BadgeUiModel) : EditionTrailingUiModel
}
