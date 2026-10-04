package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.InProgressUiModel

/** Null when the book has no read or no current edition, which is when the section has nothing to show. */
internal fun Book.toInProgress(): InProgressUiModel? {
    val read = userBookRead ?: return null
    val edition = currentEdition ?: return null

    val summaryStart: String
    val summaryEnd: String

    if (edition.isAudiobook) {
        val totalSeconds = edition.audioSeconds ?: 0
        val currentSeconds = read.currentSeconds ?: 0
        val remainingSeconds = (totalSeconds - currentSeconds).coerceAtLeast(0)

        summaryStart = "${secondsToHm(currentSeconds)} of ${secondsToHm(totalSeconds)}"
        summaryEnd = "${secondsToHm(remainingSeconds)} left"
    } else {
        val currentPage = read.currentPage ?: 0
        val pagesLeft = edition.pages?.minus(currentPage) ?: 0

        summaryStart = "p. $currentPage of ${edition.pages ?: 0}"
        summaryEnd = "$pagesLeft pages to go"
    }

    return InProgressUiModel(
        percent = percentLabel(fraction = read.progress / 100f),
        fraction = read.progress / 100f,
        summaryStart = summaryStart,
        summaryEnd = summaryEnd,
    )
}
