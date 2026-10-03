package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLine
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.uibinding.book.byline
import nl.rhaydus.softcover.core.uibinding.book.ratingPart

/** A "Recently loved" tile: the reader's rating, then the month it landed; a blank author drops the byline. */
internal fun LovedBook.toRecentlyLovedCard(): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = bookId),
    variant = BookCardVariant.Column(
        cover = BookCardCover(
            model = CoverUiModel(
                source = coverUrl
                    .takeIf { it.isNotBlank() }
                    ?.let {
                        CoverSource.Remote(
                            url = it,
                            persistEditionId = null,
                        )
                    },
                coverlessTitle = title,
                variant = CoverVariant.ProfileLovedBook,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = null,
                    bookId = bookId,
                ),
            ),
        ),
        text = BookCardContent(
            title = title,
            byline = byline(author = author),
            supportingLines = persistentListOf(
                BookCardLine(parts = persistentListOf(ratingPart(rating = ratingStars))),
                BookCardLine(parts = persistentListOf(BookCardLinePart.Text(text = monthLabel))),
            ),
        ),
        menuLabel = null,
    ),
    decorations = BookCardDecorations(
        progress = null,
        pressBehaviour = BookCardDecorations.Press.Tap,
    ),
)
