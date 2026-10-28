package nl.rhaydus.softcover.core.book.data.repository

import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DatePrecision
import nl.rhaydus.softcover.core.domain.model.JournalEventType
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.isBlank
import nl.rhaydus.softcover.core.domain.util.localDateTimeOf
import nl.rhaydus.softcover.core.domain.util.toUtcJournalTimestamp
import kotlin.time.Clock
import kotlin.time.Instant

internal fun Book.withProgress(
    newPage: Int?,
    newSeconds: Int?,
): Book {
    val existingRead = currentRead ?: return this
    val edition = currentEdition

    val totalPages = edition?.pages
    val totalSeconds = edition?.audioSeconds

    val progress: Float = when {
        newSeconds != null && totalSeconds != null && totalSeconds > 0 ->
            (newSeconds.toFloat() / totalSeconds.toFloat() * 100f).coerceIn(
                0f,
                100f,
            )

        newPage != null && totalPages != null && totalPages > 0 ->
            (newPage.toFloat() / totalPages.toFloat() * 100f).coerceIn(
                0f,
                100f,
            )

        else -> existingRead.progress
    }

    val updatedRead = existingRead.copy(
        currentPage = newPage ?: existingRead.currentPage,
        currentSeconds = newSeconds ?: existingRead.currentSeconds,
        progress = progress,
    )

    val updatedUserBook: UserBook? = userBook
        ?.withReplacedRead(read = updatedRead)
        ?.withAppendedJournal(event = JournalEventType.ProgressUpdated)

    return copy(userBook = updatedUserBook)
}

internal fun Book.withMarkedAsWantToRead(): Book {
    val existingUserBook = userBook ?: return this

    if (existingUserBook.status == BookStatus.WantToRead) return this

    val updatedUserBook: UserBook = existingUserBook.copy(status = BookStatus.WantToRead)

    return copy(userBook = updatedUserBook)
}

internal fun Book.withMarkedAsReading(): Book {
    val existingUserBook = userBook ?: return this

    // The server resumes a paused book's newest unfinished read; every other route into Reading opens a new one.
    val resumesExistingRead = existingUserBook.status == BookStatus.Paused &&
        existingUserBook.reads.any { it.isFinished.not() }

    val withReads: UserBook = if (resumesExistingRead) {
        existingUserBook
    } else {
        existingUserBook.withReplacedRead(
            read = existingUserBook.newPlaceholderRead(editionPages = readerEdition?.pages),
        )
    }

    val updatedUserBook: UserBook = withReads
        .copy(status = BookStatus.Reading)
        .withAppendedJournal(event = JournalEventType.UserBookReadStarted)

    return copy(userBook = updatedUserBook)
}

internal fun Book.withMarkedAsPaused(): Book {
    val existingUserBook = userBook ?: return this

    if (existingUserBook.status == BookStatus.Paused) return this

    val updatedUserBook: UserBook = existingUserBook.copy(status = BookStatus.Paused)

    return copy(userBook = updatedUserBook)
}

internal fun Book.withRating(rating: Double): Book {
    val existingUserBook = userBook ?: return this

    if (existingUserBook.rating == rating) return this

    val updatedUserBook: UserBook = existingUserBook.copy(rating = rating)

    return copy(userBook = updatedUserBook)
}

internal fun Book.withReview(
    review: ReviewDocument,
    hasSpoilers: Boolean,
): Book {
    val existingUserBook = userBook ?: return this

    // A blank document clears the review; the optimistic copy stores what the user wrote, which the
    // next server refresh overwrites with Hardcover's canonical `review_slate`.
    val normalizedReview: ReviewDocument? = review.takeUnless { it.isBlank() }

    val updatedUserBook: UserBook = existingUserBook.copy(
        reviewDocument = normalizedReview,
        reviewHasSpoilers = hasSpoilers,
    )

    return copy(userBook = updatedUserBook)
}

internal fun Book.withMarkedAsRead(actionAt: String?): Book {
    val existingUserBook = userBook ?: return this

    val finishedDate = actionAt
        ?.let { localDateTimeOf(actionAt = it).date.toString() }
        ?: Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

    val finishesCurrentRead = existingUserBook.status == BookStatus.Reading ||
        existingUserBook.status == BookStatus.Paused

    // From any other status no existing read is finished, so a finished placeholder keeps the book dated until the
    // server's copy, dated by the same `user_date`, replaces it.
    val finishedRead: UserBookRead = currentRead
        ?.takeIf { finishesCurrentRead && it.isFinished.not() }
        ?: existingUserBook.newPlaceholderRead(editionPages = readerEdition?.pages).copy(startedAt = null)

    val withReads: UserBook = existingUserBook.withReplacedRead(
        read = finishedRead.copy(
            finishedAt = finishedDate,
            finishedAtPrecision = DatePrecision.DAY,
            progress = 100f,
        ),
    )

    val updatedUserBook: UserBook = withReads
        .copy(status = BookStatus.Read)
        .withAppendedJournal(
            event = JournalEventType.StatusFinished,
            actionAt = actionAt,
        )

    return copy(userBook = updatedUserBook)
}

internal fun UserBook.withAppendedJournal(
    event: JournalEventType,
    actionAt: String? = null,
): UserBook {
    val updatedAt = (actionAt?.let { Instant.parse(it) } ?: Clock.System.now())
        .toUtcJournalTimestamp()

    val entry = ReadingJournal(
        updatedAt = updatedAt,
        event = event.eventName,
    )

    return copy(journals = journals + entry)
}

private fun UserBook.withReplacedRead(read: UserBookRead): UserBook {
    val isKnown = reads.any { it.id == read.id }

    val updatedReads = if (isKnown) {
        reads.map { existing -> if (existing.id == read.id) read else existing }
    } else {
        reads + read
    }

    return copy(reads = updatedReads)
}

private fun UserBook.newPlaceholderRead(editionPages: Int?): UserBookRead = UserBookRead(
    id = -id,
    currentPage = null,
    currentSeconds = null,
    progress = 0f,
    startedAt = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
    finishedAt = null,
    finishedAtPrecision = null,
    editionPages = editionPages,
)
