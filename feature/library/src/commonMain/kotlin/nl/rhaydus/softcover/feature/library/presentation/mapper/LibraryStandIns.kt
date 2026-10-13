package nl.rhaydus.softcover.feature.library.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

internal fun LibraryGridLayout.standInCards(): ImmutableList<BookCardUiModel> {
    val template = standInCard()

    return List(standInCount()) { index ->
        template.copy(key = BookCardKey.Book(bookId = -(index + 1)))
    }.toImmutableList()
}

private fun LibraryGridLayout.standInCard(): BookCardUiModel = when (this) {
    LibraryGridLayout.GRID_TWO_COLUMNS,
    LibraryGridLayout.GRID_THREE_COLUMNS,
        -> COLUMN_STAND_IN_CARD

    LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
    LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
        -> COVER_ONLY_STAND_IN_CARD

    LibraryGridLayout.LIST_COMPACT -> COMPACT_ROW_STAND_IN_CARD

    LibraryGridLayout.LIST_LARGE -> LARGE_ROW_STAND_IN_CARD
}

private val GRID_STAND_IN_DECORATIONS = BookCardDecorations(progress = BookCardProgress.Reserved)

private val COLUMN_STAND_IN_CARD = BookCardUiModel(
    key = BookCardKey.Book(bookId = -1),
    variant = BookCardVariant.Column(
        cover = standInCover(),
        text = BookCardContent(
            title = "The Name of the Wind",
            byline = "By Patrick Rothfuss",
        ),
    ),
    decorations = GRID_STAND_IN_DECORATIONS,
    isLoading = true,
)

private val COVER_ONLY_STAND_IN_CARD = BookCardUiModel(
    key = BookCardKey.Book(bookId = -2),
    variant = BookCardVariant.CoverOnly(cover = standInCover()),
    decorations = GRID_STAND_IN_DECORATIONS,
    isLoading = true,
)

private val COMPACT_ROW_STAND_IN_CARD = BookCardUiModel(
    key = BookCardKey.Book(bookId = -3),
    variant = libraryCompactRow(
        text = BookCardContent(
            title = "The Name of the Wind",
            byline = "By Patrick Rothfuss",
        ),
    ),
    isLoading = true,
)

private val LARGE_ROW_STAND_IN_CARD = BookCardUiModel(
    key = BookCardKey.Book(bookId = -4),
    variant = libraryLargeRow(
        lead = BookCardLead.Cover(cover = standInCover()),
        text = BookCardContent(
            title = "The Name of the Wind",
            eyebrow = BookCardEyebrow(text = "THE KINGKILLER CHRONICLE"),
            byline = "By Patrick Rothfuss",
            supportingLines = persistentListOf(
                persistentListOf(BookCardLinePart.Text(text = "2007 · 1.2k readers")),
            ),
        ),
    ),
    isLoading = true,
)

private fun standInCover() = BookCardCover(
    model = CoverUiModel(
        source = null,
        coverlessTitle = null,
        variant = CoverVariant.Shelf,
        isLoading = true,
    ),
)
