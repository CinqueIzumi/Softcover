package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ReadThroughRowUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.YourReadsUiModel
import kotlin.math.roundToInt

internal fun Book.toYourReads(dateStyle: DateStyle): YourReadsUiModel? {
    val userBook = userBook ?: return null

    if (userBook.reads.isEmpty()) return null

    val currentRead = userBook.currentRead
    val finishedCount = userBook.finishedReadCount

    return YourReadsUiModel(
        header = SectionHeaderUiModel.Label(eyebrow = "Your reads"),
        summary = finishedCount.takeIf { it > 0 }?.let {
            "Read $it× · ${userBook.finishYears.joinToString(separator = ", ")}"
        },
        rows = userBook.readHistory
            .map { read ->
                read.toRow(
                    isCurrent = read.id == currentRead?.id,
                    status = status,
                    dateStyle = dateStyle,
                )
            }
            .toImmutableList(),
    )
}

private fun UserBookRead.toRow(
    isCurrent: Boolean,
    status: BookStatus,
    dateStyle: DateStyle,
): ReadThroughRowUiModel {
    val started = startedAt?.toFormattedDateOrNull(dateStyle = dateStyle)
    val finished = finishedAt?.toFormattedDateOrNull(dateStyle = dateStyle)

    return ReadThroughRowUiModel(
        key = id,
        title = when {
            isCurrent && status == BookStatus.Reading -> "Reading now"
            isCurrent && status == BookStatus.Paused -> "On hold"
            isFinished -> "Finished"
            else -> "Not finished"
        },
        supporting = when {
            finished != null && started != null -> "$started – $finished"
            finished != null -> "Finished $finished"
            started != null -> "Started $started"
            else -> "No dates"
        },
        trailing = "${progress.roundToInt()}%",
    )
}

private fun String.toFormattedDateOrNull(dateStyle: DateStyle): String? = runCatching { LocalDate.parse(this) }
    .getOrNull()
    ?.let { dateStyle.formatter.format(it) }
