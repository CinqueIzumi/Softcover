package nl.rhaydus.softcover.feature.explore.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.uibinding.book.bylineColumnStandIns
import nl.rhaydus.softcover.core.uibinding.book.ratingPart

internal val BECAUSE_YOU_READ_HEADER_STAND_IN = SectionHeaderUiModel.Section(
    eyebrow = "Because you read",
    headline = "Fantasy",
    isLoading = true,
)

private const val SEARCH_RESULT_STAND_IN_COUNT = 6
private const val HIDDEN_STAND_IN_COUNT = 2

internal val TRENDING_STAND_IN_CARDS: ImmutableList<BookCardUiModel> = List(railStandInCount) { index ->
    columnStandInCard(
        bookId = -(index + 1),
        coverVariant = CoverVariant.Raised,
        text = BookCardContent(
            title = "The Name of the Wind",
            supportingLines = persistentListOf(
                persistentListOf(ratingPart(rating = 4.5)),
            ),
        ),
    )
}.toImmutableList()

internal val BECAUSE_YOU_READ_STAND_IN_CARDS: ImmutableList<BookCardUiModel> = bylineColumnStandIns(railStandInCount)

internal val RECOMMENDED_STAND_IN_CARDS: ImmutableList<BookCardUiModel> = List(railStandInCount) { index ->
    columnStandInCard(
        bookId = -(index + 1),
        coverVariant = CoverVariant.Raised,
        text = BookCardContent(
            title = "The Name of the Wind",
            byline = "By Patrick Rothfuss",
        ),
    )
}.toImmutableList()

internal val UP_NEXT_STAND_IN_CARDS: ImmutableList<BookCardUiModel> = List(railStandInCount) { index ->
    columnStandInCard(
        bookId = -(index + 1),
        coverVariant = CoverVariant.Raised,
        text = BookCardContent(
            title = "The Wise Man's Fear",
            eyebrow = BookCardEyebrow(text = "The Kingkiller Chronicle"),
            supportingLines = persistentListOf(
                persistentListOf(BookCardLinePart.Text(text = "Book #2")),
            ),
        ),
        hasMenu = true,
    )
}.toImmutableList()

internal val SEARCH_RESULT_STAND_IN_CARDS: ImmutableList<BookCardUiModel> = List(SEARCH_RESULT_STAND_IN_COUNT) { index ->
    BookCardUiModel(
        key = BookCardKey.Book(bookId = -(index + 1)),
        variant = BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Standard,
            lead = BookCardLead.Cover(
                cover = BookCardCover(
                    model = CoverUiModel(
                        source = null,
                        coverlessTitle = null,
                        variant = CoverVariant.Flat,
                        isLoading = true,
                    ),
                ),
            ),
            text = BookCardContent(
                title = "The Name of the Wind",
                eyebrow = BookCardEyebrow(text = "THE KINGKILLER CHRONICLE"),
                byline = "By Patrick Rothfuss",
                supportingLines = persistentListOf(
                    persistentListOf(BookCardLinePart.Text(text = "2007 · 1.2k readers")),
                ),
            ),
            trailing = BookCardTrailing.Bookmark.labelled(
                saved = false,
                contentDescription = "",
                enabled = false,
            ),
        ),
        isLoading = true,
    )
}.toImmutableList()

internal val HIDDEN_BOOK_STAND_IN_CARDS: ImmutableList<BookCardUiModel> = List(HIDDEN_STAND_IN_COUNT) { index ->
    BookCardUiModel(
        key = BookCardKey.Book(bookId = -(index + 1)),
        variant = BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Compact,
            lead = BookCardLead.Cover(
                cover = BookCardCover(
                    model = CoverUiModel(
                        source = null,
                        coverlessTitle = null,
                        variant = CoverVariant.Flat,
                        isLoading = true,
                    ),
                ),
            ),
            text = BookCardContent(
                title = "The Name of the Wind",
                byline = "By Patrick Rothfuss",
            ),
            trailing = BookCardTrailing.Restore,
        ),
        isLoading = true,
    )
}.toImmutableList()

internal val HIDDEN_SERIES_STAND_IN_CARDS: ImmutableList<BookCardUiModel> = List(HIDDEN_STAND_IN_COUNT) { index ->
    BookCardUiModel(
        key = BookCardKey.Series(seriesId = -(index + 1)),
        variant = BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Compact,
            lead = BookCardLead.SeriesStack(
                front = CoverUiModel(
                    source = null,
                    coverlessTitle = null,
                    variant = CoverVariant.Flat,
                    isLoading = true,
                ),
            ),
            text = BookCardContent(
                title = "The Kingkiller Chronicle",
                supportingLines = persistentListOf(
                    persistentListOf(BookCardLinePart.Text(text = "Patrick Rothfuss · 3 books")),
                ),
            ),
            trailing = BookCardTrailing.Restore,
        ),
        isLoading = true,
    )
}.toImmutableList()

private fun columnStandInCard(
    bookId: Int,
    coverVariant: CoverVariant,
    text: BookCardContent,
    hasMenu: Boolean = false,
) = BookCardUiModel(
    key = BookCardKey.Book(bookId = bookId),
    variant = BookCardVariant.Column(
        cover = BookCardCover(
            model = CoverUiModel(
                source = null,
                coverlessTitle = null,
                variant = coverVariant,
                isLoading = true,
            ),
        ),
        text = text,
        hasMenu = hasMenu,
    ),
    isLoading = true,
)
