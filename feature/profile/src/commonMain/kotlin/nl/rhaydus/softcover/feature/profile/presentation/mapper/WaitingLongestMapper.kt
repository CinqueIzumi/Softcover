package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.profile.domain.model.LongestWaiting
import nl.rhaydus.softcover.core.profile.domain.model.WaitingBook
import nl.rhaydus.softcover.core.uibinding.book.byline
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.WaitingEntry
import nl.rhaydus.softcover.feature.profile.presentation.state.WaitingLongestUiModel

private const val WAITING_TITLE = "Waiting the longest"
private const val WAITING_TRANSITION_SURFACE = "atlas-waiting"
private const val TWO_CAPTION = "Two books that have waited a while for you."
private const val ONE_CAPTION = "A book that has waited a while for you."
private const val NONE_CAPTION =
    "Books on your Want to Read shelf, and owned books you haven't started, show up here."

private val sinceFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}

/** A null [longest] yields the stand-in rows; the book waiting on the list comes before the one on the shelf. */
internal fun waitingLongestUiModel(longest: LongestWaiting?): WaitingLongestUiModel {
    if (longest == null) return loadingWaitingLongest()

    val onList = longest.onList
    val onShelf = longest.onShelf

    // The two rows can be one book, which would key two shared elements alike.
    val sharesShelfTransition = onList?.book?.id != onShelf?.book?.id

    val entries = listOfNotNull(
        onList?.waitingRow(
            prefix = "On your list since",
            sharesTransition = true,
        ),
        onShelf?.waitingRow(
            prefix = "On your shelf since",
            sharesTransition = sharesShelfTransition,
        ),
    )

    return WaitingLongestUiModel(
        title = WAITING_TITLE,
        caption = when (entries.size) {
            0 -> NONE_CAPTION
            1 -> ONE_CAPTION
            else -> TWO_CAPTION
        },
        cards = entries.toBookCardSet(),
        isLoading = false,
    )
}

private fun WaitingBook.waitingRow(
    prefix: String,
    sharesTransition: Boolean,
): Pair<BookCardUiModel, WaitingEntry> {
    val card = BookCardUiModel(
        key = BookCardKey.Book(bookId = book.id),
        variant = BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Compact,
            lead = BookCardLead.Cover(
                cover = BookCardCover(
                    model = book.toCoverUiModel(
                        variant = CoverVariant.Shelf,
                        shareTransition = sharesTransition,
                        transitionSurface = WAITING_TRANSITION_SURFACE,
                    ),
                ),
            ),
            text = BookCardContent(
                title = book.title,
                eyebrow = BookCardEyebrow(text = "$prefix ${sinceFormat.format(since)}"),
                byline = byline(author = book.authorString),
                titleTransitionKey = if (sharesTransition) {
                    bookTransitionKey(
                        element = BookTransitionElement.Title,
                        bookId = book.id,
                        surface = WAITING_TRANSITION_SURFACE,
                    )
                } else {
                    null
                },
            ),
        ),
    )

    return card to WaitingEntry(
        book = book,
        surface = WAITING_TRANSITION_SURFACE,
    )
}

private fun loadingWaitingLongest(): WaitingLongestUiModel = WaitingLongestUiModel(
    title = WAITING_TITLE,
    caption = TWO_CAPTION,
    cards = BookCardSet.standIns(
        persistentListOf(
            loadingWaitingCard(bookId = -1),
            loadingWaitingCard(bookId = -2),
        ),
    ),
    isLoading = true,
)

private fun loadingWaitingCard(bookId: Int) = BookCardUiModel(
    key = BookCardKey.Book(bookId = bookId),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Compact,
        lead = BookCardLead.Cover(
            cover = BookCardCover(
                model = CoverUiModel(
                    source = null,
                    coverlessTitle = null,
                    variant = CoverVariant.Shelf,
                    isLoading = true,
                ),
            ),
        ),
        text = BookCardContent(
            title = "Loading title",
            eyebrow = BookCardEyebrow(text = "On your list since March 2023"),
            byline = "By Author Name",
        ),
    ),
    isLoading = true,
)
