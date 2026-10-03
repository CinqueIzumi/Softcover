package nl.rhaydus.softcover.feature.library.presentation.screen.section

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLine
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.library.presentation.mapper.libraryCompactRow
import nl.rhaydus.softcover.feature.library.presentation.mapper.libraryLargeRow

internal fun LibraryGridLayout.standInCard(): BookCardUiModel = when (this) {
    LibraryGridLayout.GRID_TWO_COLUMNS,
    LibraryGridLayout.GRID_THREE_COLUMNS,
        -> COLUMN_STAND_IN_CARD

    LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
    LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
        -> COVER_ONLY_STAND_IN_CARD

    LibraryGridLayout.LIST_COMPACT -> COMPACT_ROW_STAND_IN_CARD

    LibraryGridLayout.LIST_LARGE -> LARGE_ROW_STAND_IN_CARD
}

private val STAND_IN_DECORATIONS = BookCardDecorations(pressBehaviour = BookCardDecorations.Press.None)

private val GRID_STAND_IN_DECORATIONS = STAND_IN_DECORATIONS.copy(progress = BookCardProgress.Reserved)

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
    decorations = STAND_IN_DECORATIONS,
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
                BookCardLine(parts = persistentListOf(BookCardLinePart.Text(text = "2007 · 1.2k readers"))),
            ),
        ),
    ),
    decorations = STAND_IN_DECORATIONS,
    isLoading = true,
)

private fun standInCover() = BookCardCover(
    model = CoverUiModel(
        source = null,
        coverlessTitle = null,
        variant = CoverVariant.LibraryShelfItem,
        isLoading = true,
    ),
)
