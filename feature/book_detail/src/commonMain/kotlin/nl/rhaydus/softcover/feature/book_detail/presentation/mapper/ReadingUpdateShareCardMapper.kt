package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextVariant
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.domain.model.isBlank
import nl.rhaydus.softcover.core.uibinding.progress.toPositionText
import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
import kotlin.math.roundToInt

/**
 * Builds the personalised "reading update" card, or null when there's nothing personal to share —
 * the book isn't finished or in progress, or the current user's username hasn't resolved yet.
 * The reader's rating travels on a 0–5 scale (it round-trips directly through the five-star control),
 * so it's handed to the card untouched. The review travels as a structured document so the card can
 * mask inline spoiler spans itself, exactly as the book-detail screen does.
 */
internal fun Book.toReadingUpdateShareCardUiModel(
    edition: BookEdition?,
    username: String?,
    avatarUrl: String?,
    userTags: List<UserTag> = emptyList(),
): ReadingUpdateShareCardUiModel? {
    val resolvedUsername = username?.takeIf { it.isNotBlank() } ?: return null

    val currentUserBook = userBook ?: return null

    val resolvedEdition = edition ?: defaultEdition

    val resolvedCover = resolvedEdition?.localImagePath
        ?: resolvedEdition?.url
        ?: coverUrl

    val resolvedAvatarUrl = avatarUrl?.takeIf { it.isNotBlank() }
    val resolvedAuthor = authors.firstOrNull()?.name.orEmpty()
    val resolvedTags = userTags
        .filterNot { it.spoiler }
        .map { tag ->
            ChipUiModel(
                key = tag.name,
                label = tag.name,
                interaction = ChipInteraction.Inert,
            )
        }
        .toImmutableList()

    return when (currentUserBook.status) {
        BookStatus.Read -> ReadingUpdateShareCardUiModel.Finished(
            username = resolvedUsername,
            avatarUrl = resolvedAvatarUrl,
            coverUrl = resolvedCover,
            title = title,
            author = resolvedAuthor,
            tags = resolvedTags,
            ratingStars = currentUserBook.rating?.takeIf { it > 0.0 },
            review = currentUserBook.reviewDocument
                ?.takeUnless { it.isBlank() }
                ?.toRichTextUiModel(variant = RichTextVariant.ShareCard),
        )

        BookStatus.Reading -> ReadingUpdateShareCardUiModel.Reading(
            username = resolvedUsername,
            avatarUrl = resolvedAvatarUrl,
            coverUrl = resolvedCover,
            title = title,
            author = resolvedAuthor,
            tags = resolvedTags,
            progressLabel = buildProgressLabel(
                userBookRead = userBookRead,
                edition = resolvedEdition,
            ),
        )

        else -> null
    }
}

// The position text comes from the shared formatter; the percentage is derived from the matching
// unit counts when both are known so the label is internally consistent (the raw `progress` value
// can lag the logged position). When the counts are missing it falls back to `progress`, which is
// already a 0–100 percentage.
private fun buildProgressLabel(
    userBookRead: UserBookRead?,
    edition: BookEdition?,
): String? {
    userBookRead ?: return null

    val current = if (edition?.isAudiobook == true) userBookRead.currentSeconds else userBookRead.currentPage
    val total = if (edition?.isAudiobook == true) edition.audioSeconds else edition?.pages

    if (edition == null || current == null || current <= 0 || total == null || total <= 0) {
        return userBookRead.fallbackPercentLabel()
    }

    val percent = (current.toFloat() / total * 100).roundToInt().coerceIn(
        0,
        100,
    )
    val position = userBookRead.toPositionText(
        edition = edition,
        totalPages = edition.pages ?: 0,
    ).position

    return "$position · $percent%"
}

private fun UserBookRead.fallbackPercentLabel(): String {
    val percent = progress.roundToInt().coerceIn(
        0,
        100,
    )

    return "$percent%"
}
