package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.AuthorRanking
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel

internal const val TOP_AUTHORS_TITLE = "Authors you return to"
internal const val TOP_AUTHORS_ROW_COUNT = 5
private const val WITH_ROWS_CAPTION = "Your five most-read authors, with your average rating of their books."
private const val EMPTY_ALL_TIME = "Finish a few books and the authors you read most show up here."
private const val NO_AUTHORS_CAPTION = "None of these books lists an author."

internal fun topAuthorsCaption(
    topAuthors: List<AuthorRanking>?,
    booksRead: Int,
    scope: StatsScope,
): String = when {
    topAuthors == null || topAuthors.isNotEmpty() -> WITH_ROWS_CAPTION
    booksRead == 0 -> scope.noFinishedBooksSentence(booksRead = booksRead) ?: EMPTY_ALL_TIME
    else -> NO_AUTHORS_CAPTION
}

internal fun hasTopAuthors(topAuthors: List<AuthorRanking>?): Boolean = topAuthors?.isNotEmpty() == true

/** A null [rankings] yields masked stand-in rows. */
internal fun topAuthorRows(rankings: List<AuthorRanking>?): ImmutableList<BookCardUiModel> {
    if (rankings == null) return TOP_AUTHORS_STAND_IN_ROWS

    return rankings.map { it.toRow() }.toImmutableList()
}

private fun AuthorRanking.toRow(): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = leadBook.id),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Compact,
        lead = BookCardLead.Cover(
            cover = BookCardCover(
                model = leadBook.toCoverUiModel(variant = CoverVariant.Shelf),
            ),
        ),
        text = BookCardContent(
            title = author.name,
            supportingLines = persistentListOf(
                persistentListOf(BookCardLinePart.Text(text = booksLabel(booksRead))),
            ),
        ),
        trailing = averageRating?.let { average ->
            val label = formatDecimalNumber(
                value = average,
                fractionDigits = 1,
            )

            BookCardTrailing.Rating(
                label = label,
                contentDescription = "Your average rating, $label",
            )
        },
    ),
    decorations = BookCardDecorations(pressBehaviour = BookCardDecorations.Press.None),
)

private fun booksLabel(count: Int): String = if (count == 1) "1 book" else "$count books"

private val TOP_AUTHORS_STAND_IN_TEMPLATE = BookCardUiModel(
    key = BookCardKey.Book(bookId = -1),
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
            title = "Ursula K. Le Guin",
            supportingLines = persistentListOf(
                persistentListOf(BookCardLinePart.Text(text = "4 books")),
            ),
        ),
        trailing = BookCardTrailing.Rating(
            label = "4.6",
            contentDescription = "",
        ),
    ),
    decorations = BookCardDecorations(pressBehaviour = BookCardDecorations.Press.None),
    isLoading = true,
)

private val TOP_AUTHORS_STAND_IN_ROWS: ImmutableList<BookCardUiModel> = List(TOP_AUTHORS_ROW_COUNT) { index ->
    TOP_AUTHORS_STAND_IN_TEMPLATE.copy(key = BookCardKey.Book(bookId = -(index + 1)))
}.toImmutableList()
