package nl.rhaydus.softcover.feature.library.presentation.sort

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.enum.JournalEventType
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val DATE_ADDED_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

private fun Book.dateAddedOrMin(): LocalDate = runCatching {
    LocalDate.parse(userBook?.dateAdded.orEmpty(), DATE_ADDED_FORMATTER)
}.getOrDefault(LocalDate.MIN)

private fun Book.dateFinishedOrMin(): LocalDateTime {
    userBookRead?.finishedAt?.let { finishedAt ->
        runCatching { LocalDate.parse(finishedAt).atStartOfDay() }
            .getOrNull()
            ?.let { return it }
    }

    val journals = userBook?.journals ?: return LocalDateTime.MIN
    val mostRecent = journals
        .filter { it.event == JournalEventType.StatusFinished.eventName }
        .maxByOrNull { it.updatedAt }
        ?.updatedAt ?: return LocalDateTime.MIN

    return runCatching { LocalDateTime.parse(mostRecent) }.getOrDefault(LocalDateTime.MIN)
}

private fun Book.firstAuthor(): String =
    authors.firstOrNull()?.name?.lowercase().orEmpty()

private fun Book.progressFraction(): Float =
    userBookRead?.progress ?: 0f

private fun Book.pageCount(): Int =
    currentEdition?.pages ?: defaultEdition?.pages ?: 0

// Expired deadlines yield negative days, which intentionally sort *before* future deadlines so
// overdue books float to the top of the urgency-sorted shelf.
private fun deadlineUrgency(
    deadline: BookDeadline?,
    today: LocalDate = LocalDate.now(),
): Long {
    deadline ?: return Long.MAX_VALUE

    return ChronoUnit.DAYS.between(today, deadline.deadlineDate)
}

fun List<Book>.applySort(
    mode: LibrarySortMode,
    deadlines: Map<Int, BookDeadline> = emptyMap(),
): List<Book> = when (mode) {
    LibrarySortMode.DATE_ADDED -> sortedByDescending { it.dateAddedOrMin() }
    LibrarySortMode.DATE_FINISHED -> sortedByDescending { it.dateFinishedOrMin() }
    LibrarySortMode.TITLE -> sortedBy { it.title.lowercase() }
    LibrarySortMode.AUTHOR -> sortedBy { it.firstAuthor() }
    LibrarySortMode.RATING -> sortedByDescending { it.rating }
    LibrarySortMode.PROGRESS -> sortedByDescending { it.progressFraction() }
    LibrarySortMode.PAGE_COUNT -> sortedByDescending { it.pageCount() }
    LibrarySortMode.DEADLINE_URGENCY -> sortedBy { deadlineUrgency(deadlines[it.id]) }
}

private fun BookEdition.firstAuthor(): String =
    authors.firstOrNull()?.name?.lowercase().orEmpty()

fun List<BookEdition>.applyEditionSort(mode: LibrarySortMode): List<BookEdition> = when (mode) {
    LibrarySortMode.TITLE -> sortedBy { it.title.orEmpty().lowercase() }
    LibrarySortMode.AUTHOR -> sortedBy { it.firstAuthor() }
    LibrarySortMode.PAGE_COUNT -> sortedByDescending { it.pages ?: 0 }
    LibrarySortMode.DATE_ADDED,
    LibrarySortMode.DATE_FINISHED,
    LibrarySortMode.RATING,
    LibrarySortMode.PROGRESS,
    LibrarySortMode.DEADLINE_URGENCY,
        -> this
}
