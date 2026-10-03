package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
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

/** A "Recently loved" tile: the reader's rating, then the month it landed; a blank author drops the byline. */
internal fun LovedBook.toRecentlyLovedCard(): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = bookId),
    variant = BookCardVariant.Column(
        cover = CoverUiModel(
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
        text = BookCardContent(
            title = title,
            byline = author
                .takeIf { it.isNotBlank() }
                ?.let { "By $it" },
            supportingLines = persistentListOf(
                BookCardLine(
                    parts = persistentListOf(
                        BookCardLinePart.Rating(
                            label = formatDecimalNumber(
                                value = ratingStars,
                                fractionDigits = 1,
                            ),
                        ),
                    ),
                ),
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
