package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.TimeToFinish
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ReadingRecordEntry
import nl.rhaydus.softcover.feature.profile.presentation.state.ReadingRecordsUiModel

private const val RECORDS_TITLE = "Your records"
private const val RECORDS_TRANSITION_SURFACE = "atlas-records"
private const val LONGEST_EYEBROW = "LONGEST BOOK"
private const val QUICKEST_EYEBROW = "QUICKEST READ"
private const val ONLY_LONGEST_CAPTION = "None of these reads has both a start and a finish date."
private const val ONLY_QUICKEST_CAPTION = "None of these books has a known page count."
private const val NEITHER_CAPTION = "None of these books has a page count or both reading dates."
private const val EMPTY_ALL_TIME = "Finish a few books and your records show up here."

/** A null [stats] or [timeToFinish] yields the masked stand-in. */
internal fun readingRecordsUiModel(
    stats: ReadingStats?,
    timeToFinish: TimeToFinish?,
    scope: StatsScope,
): ReadingRecordsUiModel {
    if (stats == null || timeToFinish == null) return loadingReadingRecords()

    val longest = stats.longestBook
    val quickest = timeToFinish.quickestRead

    val entries = buildList {
        if (longest != null) {
            add(
                longest.book.recordCard(
                    eyebrow = LONGEST_EYEBROW,
                    line = "${formatGroupedNumber(longest.pages)} pages",
                    sharesTransition = true,
                ),
            )
        }

        if (quickest != null) {
            add(
                quickest.book.recordCard(
                    eyebrow = QUICKEST_EYEBROW,
                    line = quickestLine(
                        pages = quickest.duration.pages,
                        days = quickest.duration.days,
                    ),
                    sharesTransition = longest?.book?.id != quickest.book.id,
                ),
            )
        }
    }

    return ReadingRecordsUiModel(
        title = RECORDS_TITLE,
        caption = recordsCaption(
            hasLongest = longest != null,
            hasQuickest = quickest != null,
            booksRead = stats.booksRead,
            scope = scope,
        ),
        cards = entries.toBookCardSet(),
        hasRecords = entries.isNotEmpty(),
        isLoading = false,
    )
}

private fun quickestLine(
    pages: Int?,
    days: Int,
): String {
    val span = if (days == 1) "1 day" else "$days days"

    return if (pages == null) "In $span" else "${formatGroupedNumber(pages)} pages in $span"
}

private fun recordsCaption(
    hasLongest: Boolean,
    hasQuickest: Boolean,
    booksRead: Int,
    scope: StatsScope,
): String = when {
    hasLongest && hasQuickest -> ""
    hasLongest -> ONLY_LONGEST_CAPTION
    hasQuickest -> if (booksRead > 0) ONLY_QUICKEST_CAPTION else ""
    booksRead > 0 -> NEITHER_CAPTION
    else -> scope.scopePhrase()?.let { "No finished reads $it." } ?: EMPTY_ALL_TIME
}

private fun Book.recordCard(
    eyebrow: String,
    line: String,
    sharesTransition: Boolean,
): Pair<BookCardUiModel, ReadingRecordEntry> {
    val card = BookCardUiModel(
        key = BookCardKey.Book(bookId = id),
        variant = BookCardVariant.Tile(
            cover = BookCardCover(
                model = toCoverUiModel(
                    variant = CoverVariant.Raised,
                    shareTransition = sharesTransition,
                    transitionSurface = RECORDS_TRANSITION_SURFACE,
                ),
            ),
            text = BookCardContent(
                title = title,
                eyebrow = BookCardEyebrow(text = eyebrow),
                supportingLines = persistentListOf(persistentListOf(BookCardLinePart.Text(text = line))),
                titleTransitionKey = if (sharesTransition) {
                    bookTransitionKey(
                        element = BookTransitionElement.Title,
                        bookId = id,
                        surface = RECORDS_TRANSITION_SURFACE,
                    )
                } else {
                    null
                },
            ),
        ),
    )

    return card to ReadingRecordEntry(
        book = this,
        surface = RECORDS_TRANSITION_SURFACE,
    )
}

private fun loadingReadingRecords(): ReadingRecordsUiModel = ReadingRecordsUiModel(
    title = RECORDS_TITLE,
    caption = "",
    cards = BookCardSet.standIns(
        listOf(
            loadingRecordCard(
                bookId = -1,
                eyebrow = LONGEST_EYEBROW,
                line = "000 pages",
            ),
            loadingRecordCard(
                bookId = -2,
                eyebrow = QUICKEST_EYEBROW,
                line = "000 pages in 0 days",
            ),
        ).toImmutableList(),
    ),
    hasRecords = false,
    isLoading = true,
)

private fun loadingRecordCard(
    bookId: Int,
    eyebrow: String,
    line: String,
) = BookCardUiModel(
    key = BookCardKey.Book(bookId = bookId),
    variant = BookCardVariant.Tile(
        cover = BookCardCover(
            model = CoverUiModel(
                source = null,
                coverlessTitle = null,
                variant = CoverVariant.Raised,
                isLoading = true,
            ),
        ),
        text = BookCardContent(
            title = "Loading title",
            eyebrow = BookCardEyebrow(text = eyebrow),
            supportingLines = persistentListOf(persistentListOf(BookCardLinePart.Text(text = line))),
        ),
    ),
    isLoading = true,
)
