package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.uibinding.book.byline
import nl.rhaydus.softcover.core.uibinding.book.ratingPart
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.RecentlyLovedEntry

private val monthYearLabelFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
}

/**
 * A "Recently loved" tile: the reader's rating, then the month it landed; a blank author drops the byline.
 * The cover is the library book's, so it follows the picked edition.
 */
internal fun RecentlyLovedEntry.toRecentlyLovedCard(): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = loved.bookId),
    variant = BookCardVariant.Column(
        cover = BookCardCover(
            model = libraryBook.toCoverUiModel(
                variant = CoverVariant.Raised,
                shareTransition = true,
            ),
        ),
        text = BookCardContent(
            title = libraryBook.title,
            titleTransitionKey = bookTransitionKey(
                element = BookTransitionElement.Title,
                bookId = loved.bookId,
            ),
            byline = byline(author = libraryBook.authorString),
            supportingLines = persistentListOf(
                persistentListOf(ratingPart(rating = loved.ratingStars)),
                persistentListOf(BookCardLinePart.Text(text = monthYearLabelFormat.format(loved.finishedOn))),
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
