package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

/** `null` when the book has no current edition or [deadline] is `null`. */
internal fun Book.toDeadlineProgress(deadline: BookDeadline?): DeadlineProgress? {
    val edition = currentEdition ?: return null
    val bookDeadline = deadline ?: return null

    val current = when (bookDeadline.unit) {
        DeadlineUnit.PAGES -> currentRead?.currentPage ?: 0
        DeadlineUnit.SECONDS -> currentRead?.currentSeconds ?: 0
    }
    val total = when (bookDeadline.unit) {
        DeadlineUnit.PAGES -> edition.pages ?: 0
        DeadlineUnit.SECONDS -> edition.audioSeconds ?: 0
    }

    return DeadlineProgress.compute(
        deadline = bookDeadline,
        current = current,
        total = total,
    )
}
