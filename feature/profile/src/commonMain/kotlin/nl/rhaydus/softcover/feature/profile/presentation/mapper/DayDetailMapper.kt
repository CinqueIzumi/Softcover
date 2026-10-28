package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDay
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDayBook
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasStreaks
import nl.rhaydus.softcover.feature.profile.presentation.state.DayDetailBook
import nl.rhaydus.softcover.feature.profile.presentation.state.DayDetailUiModel

private const val MISSING_BOOK_TITLE = "A book no longer in your library"

private val clockFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
}

private val dayFormat = LocalDate.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_FULL)
    char(' ')
    day(padding = Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
}

private val dayWithYearFormat = LocalDate.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_FULL)
    char(' ')
    day(padding = Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}

/**
 * The year joins the eyebrow only when [currentYear] is not the day's own.
 * A book absent from [books] gets an untappable row.
 */
internal fun dayDetailUiModel(
    day: ReadingDay,
    books: List<Book>,
    currentYear: Int,
    streaks: AtlasStreaks,
): DayDetailUiModel {
    val booksById = books.associateBy { it.id }
    val totalPages = day.books.sumOf { it.pages }
    val eyebrowFormat = if (day.date.year == currentYear) dayFormat else dayWithYearFormat

    return DayDetailUiModel(
        scaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = eyebrowFormat.format(day.date),
                    headline = "${pagesLabel(totalPages)} across ${booksLabel(day.books.size)}",
                    description = streakDescription(
                        day = day.date,
                        streaks = streaks,
                    ),
                ),
            ),
            footer = SheetFooterUiModel.Stacked(
                primary = SheetFooterButton(
                    label = "Done",
                    dismissesSheet = true,
                ),
                secondary = null,
            ),
        ),
        books = day.books
            .map { dayBook -> dayBook.toRow(book = booksById[dayBook.bookId]) }
            .toBookCardSet(),
    )
}

private fun streakDescription(
    day: LocalDate,
    streaks: AtlasStreaks,
): String? {
    val streak = streaks.activity.streakOn(day)?.takeIf { it.days >= 2 } ?: return null
    val position = streak.start.daysUntil(day) + 1

    return when (streak) {
        streaks.longestInAtlasYear -> "Day $position of your longest streak."
        streaks.current -> "Day $position of your current streak."
        else -> "Day $position of a ${streak.days}-day streak."
    }
}

private fun ReadingDayBook.toRow(book: Book?): Pair<BookCardUiModel, DayDetailBook> {
    val trailing = BookCardTrailing.Badge(
        model = BadgeUiModel(
            label = pagesLabel(pages),
            tone = BadgeTone.Neutral,
        ),
    )

    val text = BookCardContent(
        title = book?.title ?: MISSING_BOOK_TITLE,
        supportingLines = persistentListOf(
            persistentListOf(BookCardLinePart.Text(text = caption())),
        ),
    )

    val card = BookCardUiModel(
        key = BookCardKey.Book(bookId = bookId),
        variant = BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Compact,
            lead = book?.let {
                BookCardLead.Cover(
                    cover = BookCardCover(model = it.toCoverUiModel(variant = CoverVariant.Shelf)),
                )
            },
            text = text,
            trailing = trailing,
        ),
        decorations = BookCardDecorations(
            pressBehaviour = if (book == null) BookCardDecorations.Press.None else BookCardDecorations.Press.Tap,
        ),
    )

    return card to DayDetailBook(
        bookId = bookId,
        initialCover = book?.let { BookInitialCover.fromBook(it) },
    )
}

private fun ReadingDayBook.caption(): String = listOfNotNull(
    if (fromPage == 0) "Up to page $toPage" else "Pages $fromPage to $toPage",
    updatesLabel(),
    "finished".takeIf { finished },
).joinToString(separator = " · ")

private fun ReadingDayBook.updatesLabel(): String? {
    val first = firstUpdateAt?.let(clockFormat::format)
    val last = lastUpdateAt?.let(clockFormat::format)
    val count = countLabel(updateCount)

    return when {
        updateCount == 0 -> null
        first == null || last == null -> count
        first == last -> "$count at $first"
        else -> "$count, $first to $last"
    }
}

private fun countLabel(count: Int): String = if (count == 1) "1 update" else "$count updates"

private fun pagesLabel(count: Int): String = if (count == 1) "1 page" else "$count pages"

private fun booksLabel(count: Int): String = if (count == 1) "1 book" else "$count books"
