package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShelveReadingStatusUiModel
import kotlin.math.roundToInt

internal fun Book.toShelveReadingStatus(): ShelveReadingStatusUiModel? {
    val read = currentRead ?: return null
    val isAudiobook = currentEdition?.isAudiobook == true

    return ShelveReadingStatusUiModel(
        percent = StatNumberUiModel(
            value = read.progress.coerceIn(
                minimumValue = 0f,
                maximumValue = 100f,
            ).roundToInt().toDouble(),
            format = StatNumberFormat.Plain,
            variant = StatNumberVariant.Label,
            suffix = StatNumberSuffix.Fixed("%"),
        ),
        position = if (isAudiobook) {
            StatNumberUiModel(
                value = (read.currentSeconds ?: 0).toDouble(),
                format = StatNumberFormat.Duration,
                variant = StatNumberVariant.Label,
                prefix = " · ",
            )
        } else {
            StatNumberUiModel(
                value = (read.currentPage ?: 0).toDouble(),
                format = StatNumberFormat.Plain,
                variant = StatNumberVariant.Label,
                prefix = " · p. ",
            )
        },
    )
}
