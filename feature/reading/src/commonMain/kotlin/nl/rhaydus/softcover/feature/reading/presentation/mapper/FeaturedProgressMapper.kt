package nl.rhaydus.softcover.feature.reading.presentation.mapper

import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.component.progress.ProgressBarUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedProgressUiModel

internal fun Book.toFeaturedProgress(): FeaturedProgressUiModel {
    val progress = userBookRead?.progress ?: 0f
    val edition = currentEdition

    val pageLead: String
    val pageTail: String?

    if (edition?.isAudiobook == true) {
        pageLead = secondsToHm(userBookRead?.currentSeconds ?: 0)
        pageTail = edition.audioSeconds?.let { " of ${secondsToHm(it)}" }
    } else {
        pageLead = "Page ${userBookRead?.currentPage ?: 0}"
        pageTail = (edition?.pages ?: defaultEdition?.pages)?.let { " of $it" }
    }

    return FeaturedProgressUiModel(
        percent = percentLabel(fraction = progress / 100f),
        pageLead = pageLead,
        pageTail = pageTail,
        bar = ProgressBarUiModel(
            fraction = (progress / 100f),
        ),
    )
}
