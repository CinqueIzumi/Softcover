package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.progress.toPositionText
import nl.rhaydus.softcover.feature.book_detail.presentation.state.InProgressUiModel
import kotlin.math.roundToInt

/** The section's opener: "On hold" for a paused book, "In progress" otherwise. */
internal fun Book.toInProgressSectionHeader(): SectionHeaderUiModel =
    SectionHeaderUiModel.Section(eyebrow = if (isPaused) "On hold" else "In progress")

/** Null when the book has no read or no current edition, which is when the section has nothing to show. */
internal fun Book.toInProgress(): InProgressUiModel? {
    val read = userBookRead ?: return null
    val text = toPositionText() ?: return null

    return InProgressUiModel(
        percent = StatNumberUiModel(
            value = read.progress.coerceIn(
                minimumValue = 0f,
                maximumValue = 100f,
            ).roundToInt().toDouble(),
            format = StatNumberFormat.Plain,
            variant = StatNumberVariant.Hero,
            suffix = StatNumberSuffix.Fixed("%"),
        ),
        fraction = read.progress / 100f,
        summaryStart = text.position,
        summaryEnd = text.remaining,
    )
}
