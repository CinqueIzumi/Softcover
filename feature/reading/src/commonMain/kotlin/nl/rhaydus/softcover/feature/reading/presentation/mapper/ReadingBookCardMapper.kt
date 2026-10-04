package nl.rhaydus.softcover.feature.reading.presentation.mapper

import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.uibinding.book.byline
import nl.rhaydus.softcover.core.uibinding.book.seriesEyebrow
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.core.uibinding.deadline.toDeadlineSummaryUiModel
import nl.rhaydus.softcover.core.uibinding.format.percentLabel

/** An "Also reading" row; a failed save replaces the series eyebrow and shakes the row. */
internal fun Book.toAlsoReadingCard(
    deadlineProgress: DeadlineProgress?,
    coverBadge: CoverOverlayUiModel?,
    dateStyle: DateStyle,
    mutationFailed: Boolean,
): BookCardUiModel {
    val progress = userBookRead?.progress ?: 0f

    val fraction = progress / 100f

    val eyebrow = if (mutationFailed) {
        BookCardEyebrow(
            text = "Couldn't save — tap to retry".uppercase(),
            tone = BookCardEyebrow.Tone.Error,
        )
    } else {
        seriesEyebrow(seriesText = seriesText)
    }

    return BookCardUiModel(
        key = BookCardKey.Book(bookId = id),
        variant = BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Standard,
            lead = BookCardLead.Cover(
                cover = BookCardCover(
                    model = toCoverUiModel(
                        variant = CoverVariant.Tile,
                        shareTransition = true,
                    ),
                    badge = coverBadge,
                ),
            ),
            text = BookCardContent(
                title = title,
                eyebrow = eyebrow,
                byline = byline(author = authorString),
            ),
            trailing = BookCardTrailing.SetProgress.labelled(),
            deadline = deadlineProgress?.toDeadlineSummaryUiModel(
                dateStyle = dateStyle,
            ),
            contentHorizontalInset = 24.dp,
            contentVerticalInset = 18.dp,
        ),
        decorations = BookCardDecorations(
            progress = BookCardProgress.Value(
                fraction = fraction,
                label = percentLabel(fraction = fraction),
            ),
            shakeOnError = mutationFailed,
        ),
    )
}

/** A "Pick up next" tile: cover and title only, no progress. */
internal fun Book.toPickUpNextCard(): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = id),
    variant = BookCardVariant.Column(
        cover = BookCardCover(
            model = toCoverUiModel(
                variant = CoverVariant.Tile,
                shareTransition = true,
            ),
        ),
        text = BookCardContent(title = title),
    ),
)
