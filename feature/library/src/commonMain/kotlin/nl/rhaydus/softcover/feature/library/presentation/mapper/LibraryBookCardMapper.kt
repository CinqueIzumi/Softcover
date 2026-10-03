package nl.rhaydus.softcover.feature.library.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryTone
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.uibinding.book.bookStatsLine
import nl.rhaydus.softcover.core.uibinding.deadline.toBadgeUiModel
import nl.rhaydus.softcover.core.uibinding.deadline.toDeadlineSummaryUiModel
import kotlin.math.roundToInt

/**
 * The book's card for [layout]: column or cover-only on the grids, a compact or large row on the
 * lists. [selection] is `null` outside selection mode.
 */
internal fun Book.toBookCard(
    layout: LibraryGridLayout,
    press: BookCardDecorations.Press,
    selection: SelectionMarkUiModel?,
    deadline: BookDeadline?,
    dateStyle: DateStyle,
): BookCardUiModel {
    val deadlineProgress = toDeadlineProgress(deadline)

    val variant: BookCardVariant
    val decorations: BookCardDecorations

    when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> {
            variant = BookCardVariant.CoverOnly(cover = toShelfBookCover())
            decorations = gridDecorations(
                coverBadge = deadlineProgress?.toCountdownCoverOverlayUiModel(),
                selection = selection,
                press = press,
            )
        }

        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS,
            -> {
            variant = BookCardVariant.Column(
                cover = toShelfBookCover(),
                text = BookCardContent(
                    title = title,
                    byline = authors.firstAuthorByline(),
                ),
            )
            decorations = gridDecorations(
                coverBadge = deadlineProgress?.toCountdownCoverOverlayUiModel(),
                selection = selection,
                press = press,
            )
        }

        LibraryGridLayout.LIST_COMPACT -> {
            variant = BookCardVariant.Row(
                size = BookCardVariant.Row.Size.Compact,
                lead = null,
                text = BookCardContent(
                    title = title,
                    byline = authors.firstAuthorByline(),
                ),
                trailing = deadlineProgress?.toBadgeUiModel()?.let { BookCardTrailing.Badge(model = it) },
            )
            decorations = BookCardDecorations(
                selection = selection,
                pressBehaviour = press,
            )
        }

        LibraryGridLayout.LIST_LARGE -> {
            variant = BookCardVariant.Row(
                size = BookCardVariant.Row.Size.Large,
                lead = BookCardLead.Cover(model = toShelfBookCover()),
                text = BookCardContent(
                    title = title,
                    eyebrow = seriesText
                        ?.takeIf { it.isNotBlank() }
                        ?.let { BookCardEyebrow(text = it.uppercase()) },
                    byline = currentEdition?.authorString
                        ?.takeIf { it.isNotBlank() }
                        ?.let { "By $it" },
                    supportingLines = bookStatsLine(
                        releaseYear = releaseYear,
                        usersCount = usersCount,
                        rating = rating,
                    )?.let { persistentListOf(it) } ?: persistentListOf(),
                ),
                deadline = deadlineProgress?.toDeadlineSummaryUiModel(
                    dateStyle = dateStyle,
                    tone = DeadlineSummaryTone.OnSurface,
                ),
            )
            decorations = BookCardDecorations(
                progress = labelledProgress(),
                selection = selection,
                pressBehaviour = press,
            )
        }
    }

    return BookCardUiModel(
        key = BookCardKey.Book(bookId = id),
        variant = variant,
        decorations = decorations,
    )
}

/** The edition's card for [layout]; editions carry no selection, progress or deadline on the lists. */
internal fun BookEdition.toEditionCard(
    layout: LibraryGridLayout,
    press: BookCardDecorations.Press,
): BookCardUiModel {
    val text = BookCardContent(
        title = title.orEmpty(),
        byline = authors.firstAuthorByline(),
    )

    val variant = when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> BookCardVariant.CoverOnly(cover = toShelfEditionCover())

        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS,
            -> BookCardVariant.Column(
            cover = toShelfEditionCover(),
            text = text,
        )

        LibraryGridLayout.LIST_COMPACT -> BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Compact,
            lead = null,
            text = text,
        )

        LibraryGridLayout.LIST_LARGE -> BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Large,
            lead = BookCardLead.Cover(model = toShelfEditionCover()),
            text = text,
        )
    }

    val isList = layout == LibraryGridLayout.LIST_COMPACT || layout == LibraryGridLayout.LIST_LARGE

    return BookCardUiModel(
        key = BookCardKey.Edition(editionId = id),
        variant = variant,
        decorations = BookCardDecorations(
            progress = if (isList) null else BookCardProgress.Reserved,
            pressBehaviour = press,
        ),
    )
}

private fun Book.gridDecorations(
    coverBadge: CoverOverlayUiModel?,
    selection: SelectionMarkUiModel?,
    press: BookCardDecorations.Press,
): BookCardDecorations {
    val progress = userBookRead?.progress

    return BookCardDecorations(
        progress = if (status == BookStatus.Reading && progress != null) {
            BookCardProgress.Value(
                fraction = progress / 100f,
                label = null,
            )
        } else {
            BookCardProgress.Reserved
        },
        coverBadge = coverBadge,
        selection = selection,
        pressBehaviour = press,
    )
}

private fun Book.labelledProgress(): BookCardProgress.Value? {
    val progress = userBookRead?.progress

    if (status != BookStatus.Reading || progress == null) return null

    val fraction = progress / 100f

    return BookCardProgress.Value(
        fraction = fraction,
        label = "${(fraction * 100).roundToInt()}%",
    )
}

private fun List<Author>.firstAuthorByline(): String? = firstOrNull()
    ?.name
    ?.takeIf { it.isNotBlank() }
    ?.let { "By $it" }
