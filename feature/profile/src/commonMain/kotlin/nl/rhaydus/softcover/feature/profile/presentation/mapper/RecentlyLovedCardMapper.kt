package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.uibinding.book.byline
import nl.rhaydus.softcover.core.uibinding.book.ratingPart

/** A "Recently loved" tile: the reader's rating, then the month it landed; a blank author drops the byline. */
internal fun LovedBook.toRecentlyLovedCard(): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = bookId),
    variant = BookCardVariant.Column(
        cover = BookCardCover(
            model = CoverUiModel(
                source = coverUrl
                    .takeIf { it.isNotBlank() }
                    ?.let {
                        CoverSource.Remote(
                            url = it,
                            persistEditionId = null,
                        )
                    },
                coverlessTitle = title,
                variant = CoverVariant.Raised,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = null,
                    bookId = bookId,
                ),
            ),
        ),
        text = BookCardContent(
            title = title,
            byline = byline(author = author),
            supportingLines = persistentListOf(
                persistentListOf(ratingPart(rating = ratingStars)),
                persistentListOf(BookCardLinePart.Text(text = monthLabel)),
            ),
        ),
    ),
)

internal const val RECENTLY_LOVED_CARD_COUNT = 3

private val RECENTLY_LOVED_STAND_IN_TEMPLATE = BookCardUiModel(
    key = BookCardKey.Book(bookId = -1),
    variant = BookCardVariant.Column(
        cover = BookCardCover(
            model = CoverUiModel(
                source = null,
                coverlessTitle = null,
                variant = CoverVariant.Raised,
                isLoading = true,
            ),
        ),
        text = BookCardContent(
            title = "The Name of the Wind",
            byline = "By Patrick Rothfuss",
            supportingLines = persistentListOf(
                persistentListOf(ratingPart(rating = 4.5)),
                persistentListOf(BookCardLinePart.Text(text = "May 2026")),
            ),
        ),
    ),
    isLoading = true,
)

internal val RECENTLY_LOVED_STAND_IN_CARDS: ImmutableList<BookCardUiModel> = List(RECENTLY_LOVED_CARD_COUNT) { index ->
    RECENTLY_LOVED_STAND_IN_TEMPLATE.copy(key = BookCardKey.Book(bookId = -(index + 1)))
}.toImmutableList()
