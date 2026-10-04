package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.core.uibinding.progress.toPositionText
import nl.rhaydus.softcover.feature.book_detail.presentation.state.InProgressUiModel

/** Null when the book has no read or no current edition, which is when the section has nothing to show. */
internal fun Book.toInProgress(): InProgressUiModel? {
    val read = userBookRead ?: return null
    val text = toPositionText() ?: return null

    return InProgressUiModel(
        percent = percentLabel(fraction = read.progress / 100f),
        fraction = read.progress / 100f,
        summaryStart = text.position,
        summaryEnd = text.remaining,
    )
}
