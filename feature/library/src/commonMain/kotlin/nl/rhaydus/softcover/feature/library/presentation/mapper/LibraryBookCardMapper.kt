package nl.rhaydus.softcover.feature.library.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardSelection
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.uibinding.book.bookStatsLine
import nl.rhaydus.softcover.core.uibinding.book.byline
import nl.rhaydus.softcover.core.uibinding.book.seriesEyebrow
import nl.rhaydus.softcover.core.uibinding.book.transitionKey
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.core.uibinding.deadline.toBadgeUiModel
import nl.rhaydus.softcover.core.uibinding.deadline.toDeadlineSummaryUiModel
import nl.rhaydus.softcover.core.uibinding.format.percentLabel

/**
 * The book's card for [layout]: column or cover-only on the grids, a compact or large row on the
 * lists. [selection] is `null` outside selection mode.
 */
internal fun Book.toBookCard(
    layout: LibraryGridLayout,
    press: BookCardDecorations.Press,
    selection: BookCardSelection?,
    deadline: BookDeadline?,
    dateStyle: DateStyle,
): BookCardUiModel {
    val deadlineProgress = toDeadlineProgress(deadline)
    val badgedDeadline = deadlineProgress.takeUnless { isPaused }
    val deadlineSummary = deadlineProgress?.toDeadlineSummaryUiModel(
        dateStyle = dateStyle,
        isPaused = isPaused,
    )

    val variant: BookCardVariant
    val decorations: BookCardDecorations

    when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> {
            variant = BookCardVariant.CoverOnly(
                cover = BookCardCover(
                    model = toCoverUiModel(
                        variant = CoverVariant.Shelf,
                        shareTransition = true,
                    ),
                    badge = badgedDeadline?.toCountdownCoverOverlayUiModel(),
                ),
            )
            decorations = gridDecorations(
                selection = selection,
                press = press,
            )
        }

        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS,
            -> {
            variant = BookCardVariant.Column(
                cover = BookCardCover(
                    model = toCoverUiModel(
                        variant = CoverVariant.Shelf,
                        shareTransition = true,
                    ),
                    badge = badgedDeadline?.toCountdownCoverOverlayUiModel(),
                ),
                text = BookCardContent(
                    title = title,
                    byline = byline(author = authorString),
                    titleTransitionKey = transitionKey(
                        element = BookTransitionElement.Title,
                    ),
                ),
            )
            decorations = gridDecorations(
                selection = selection,
                press = press,
            )
        }

        LibraryGridLayout.LIST_COMPACT -> {
            variant = libraryCompactRow(
                text = BookCardContent(
                    title = title,
                    byline = byline(author = authorString),
                    titleTransitionKey = transitionKey(
                        element = BookTransitionElement.Title,
                    ),
                ),
                trailing = badgedDeadline?.toBadgeUiModel()?.let { BookCardTrailing.Badge(model = it) },
            )
            decorations = BookCardDecorations(
                selection = selection,
                pressBehaviour = press,
            )
        }

        LibraryGridLayout.LIST_LARGE -> {
            variant = libraryLargeRow(
                lead = BookCardLead.Cover(
                    cover = BookCardCover(
                        model = toCoverUiModel(
                            variant = CoverVariant.Shelf,
                            shareTransition = true,
                        ),
                    ),
                ),
                text = BookCardContent(
                    title = title,
                    eyebrow = seriesEyebrow(seriesText = seriesText),
                    byline = byline(author = authorString),
                    titleTransitionKey = transitionKey(
                        element = BookTransitionElement.Title,
                    ),
                    supportingLines = bookStatsLine(
                        releaseYear = releaseYear,
                        usersCount = usersCount,
                        rating = rating,
                        ratingTransitionKey = transitionKey(
                            element = BookTransitionElement.Rating,
                        ),
                    )?.let { persistentListOf(it) } ?: persistentListOf(),
                ),
                deadline = deadlineSummary,
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
        byline = byline(author = authorString),
    )

    val variant = when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> BookCardVariant.CoverOnly(cover = BookCardCover(model = toShelfEditionCover()))

        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS,
            -> BookCardVariant.Column(
            cover = BookCardCover(model = toShelfEditionCover()),
            text = text,
        )

        LibraryGridLayout.LIST_COMPACT -> libraryCompactRow(text = text)

        LibraryGridLayout.LIST_LARGE -> libraryLargeRow(
            lead = BookCardLead.Cover(cover = BookCardCover(model = toShelfEditionCover())),
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

internal fun libraryCompactRow(
    text: BookCardContent,
    trailing: BookCardTrailing? = null,
): BookCardVariant.Row = BookCardVariant.Row(
    size = BookCardVariant.Row.Size.Compact,
    lead = null,
    text = text,
    trailing = trailing,
    contentVerticalInset = Spacing.sm,
)

internal fun libraryLargeRow(
    lead: BookCardLead,
    text: BookCardContent,
    deadline: DeadlineSummaryUiModel? = null,
): BookCardVariant.Row = BookCardVariant.Row(
    size = BookCardVariant.Row.Size.Large,
    lead = lead,
    text = text,
    deadline = deadline,
    contentVerticalInset = Spacing.md,
)

private fun Book.gridDecorations(
    selection: BookCardSelection?,
    press: BookCardDecorations.Press,
): BookCardDecorations {
    val progress = userBookRead?.progress

    return BookCardDecorations(
        progress = if ((status == BookStatus.Reading || status == BookStatus.Paused) && progress != null) {
            BookCardProgress.Value(
                fraction = progress / 100f,
                label = null,
            )
        } else {
            BookCardProgress.Reserved
        },
        selection = selection,
        pressBehaviour = press,
    )
}

private fun Book.labelledProgress(): BookCardProgress.Value? {
    val progress = userBookRead?.progress

    if ((status != BookStatus.Reading && status != BookStatus.Paused) || progress == null) return null

    val fraction = progress / 100f

    return BookCardProgress.Value(
        fraction = fraction,
        label = percentLabel(fraction = fraction),
    )
}
