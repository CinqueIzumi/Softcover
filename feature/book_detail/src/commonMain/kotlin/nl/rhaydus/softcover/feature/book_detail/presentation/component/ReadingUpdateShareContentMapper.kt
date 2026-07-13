package nl.rhaydus.softcover.feature.book_detail.presentation.component

import kotlin.math.roundToInt
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.designsystem.presentation.share.ReadingUpdateKind
import nl.rhaydus.softcover.core.designsystem.presentation.share.ReadingUpdateShareContent
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.domain.model.isBlank

/**
 * Builds the personalised "reading update" card, or null when there's nothing personal to share —
 * the book isn't finished or in progress, or the current user's username hasn't resolved yet.
 * The reader's rating travels on a 0–5 scale (it round-trips directly through the five-star control),
 * so it's handed to the card untouched. The review travels as a structured document so the card can
 * mask inline spoiler spans itself, exactly as the book-detail screen does.
 */
internal fun Book.toReadingUpdateContent(
    edition: BookEdition?,
    username: String?,
    avatarUrl: String?,
    userTags: List<UserTag> = emptyList(),
): ReadingUpdateShareContent? {
    val resolvedUsername = username?.takeIf { it.isNotBlank() } ?: return null

    val currentUserBook = userBook ?: return null

    val kind = when (currentUserBook.status) {
        BookStatus.Read -> ReadingUpdateKind.FINISHED
        BookStatus.Reading -> ReadingUpdateKind.READING
        else -> return null
    }

    val resolvedEdition = edition ?: defaultEdition

    val resolvedCover = resolvedEdition?.localImagePath
        ?: resolvedEdition?.url
        ?: coverUrl

    return ReadingUpdateShareContent(
        username = resolvedUsername,
        avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
        coverUrl = resolvedCover,
        title = title,
        author = authors.firstOrNull()?.name.orEmpty(),
        kind = kind,

        ratingStars = if (kind == ReadingUpdateKind.FINISHED) {
            currentUserBook.rating?.takeIf { it > 0.0 }
        } else {
            null
        },

        review = if (kind == ReadingUpdateKind.FINISHED) {
            currentUserBook.reviewDocument?.takeUnless { it.isBlank() }
        } else {
            null
        },

        progressLabel = if (kind == ReadingUpdateKind.READING) {
            buildProgressLabel(
                userBookRead = userBookRead,
                edition = resolvedEdition,
            )
        } else {
            null
        },

        tags = userTags.filterNot { it.spoiler }.map { it.name },
    )
}

// Mirrors the book-detail progress block: audiobooks are tracked in time (listened / total), print
// editions in pages. The percentage is derived from the matching unit counts when both are known so
// the label is internally consistent (the raw `progress` value can lag the logged position); when
// the counts are missing it falls back to `progress`, which is already a 0–100 percentage.
private fun buildProgressLabel(
    userBookRead: UserBookRead?,
    edition: BookEdition?,
): String? {
    userBookRead ?: return null

    if (edition?.isAudiobook == true) {
        val currentSeconds = userBookRead.currentSeconds?.takeIf { it > 0 }
        val totalSeconds = edition.audioSeconds?.takeIf { it > 0 }

        if (currentSeconds != null && totalSeconds != null) {
            val percent = (currentSeconds.toFloat() / totalSeconds * 100).roundToInt().coerceIn(
                0,
                100,
            )

            return "${secondsToHm(currentSeconds)} / ${secondsToHm(totalSeconds)} · $percent%"
        }

        return userBookRead.fallbackPercentLabel()
    }

    val currentPage = userBookRead.currentPage?.takeIf { it > 0 }
    val totalPages = edition?.pages?.takeIf { it > 0 }

    if (currentPage != null && totalPages != null) {
        val percent = (currentPage.toFloat() / totalPages * 100).roundToInt().coerceIn(
            0,
            100,
        )

        return "page $currentPage / $totalPages · $percent%"
    }

    return userBookRead.fallbackPercentLabel()
}

private fun UserBookRead.fallbackPercentLabel(): String {
    val percent = progress.roundToInt().coerceIn(
        0,
        100,
    )

    return "$percent%"
}
