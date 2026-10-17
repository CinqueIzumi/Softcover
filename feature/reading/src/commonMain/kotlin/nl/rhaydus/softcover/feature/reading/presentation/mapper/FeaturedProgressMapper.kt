package nl.rhaydus.softcover.feature.reading.presentation.mapper

import kotlin.math.roundToInt
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.progress.resolvedTotalPages
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedProgressUiModel

internal fun Book.toFeaturedProgress(): FeaturedProgressUiModel {
    val progress = currentRead?.progress ?: 0f
    val edition = currentEdition

    val pageLead: StatNumberUiModel
    val pageTail: String?

    if (edition?.isAudiobook == true) {
        pageLead = StatNumberUiModel(
            value = (currentRead?.currentSeconds ?: 0).toDouble(),
            format = StatNumberFormat.Duration,
            variant = StatNumberVariant.BodyStrong,
        )
        pageTail = edition.audioSeconds?.let { " of ${secondsToHm(it)}" }
    } else {
        pageLead = StatNumberUiModel(
            value = (currentRead?.currentPage ?: 0).toDouble(),
            format = StatNumberFormat.Plain,
            variant = StatNumberVariant.BodyStrong,
            prefix = "Page ",
        )
        pageTail = resolvedTotalPages().takeIf { it > 0 }?.let { " of $it" }
    }

    return FeaturedProgressUiModel(
        percent = StatNumberUiModel(
            value = progress.coerceIn(
                minimumValue = 0f,
                maximumValue = 100f,
            ).roundToInt().toDouble(),
            format = StatNumberFormat.Plain,
            variant = StatNumberVariant.Headline,
            suffix = StatNumberSuffix.Fixed("%"),
        ),
        pageLead = pageLead,
        pageTail = pageTail,
        fraction = progress / 100f,
    )
}
