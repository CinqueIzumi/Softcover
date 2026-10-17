package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HeroEditionUiModel

internal fun BookEdition.toHeroEditionUiModel(): HeroEditionUiModel {
    val length = if (isAudiobook) {
        audioSeconds?.takeIf { it > 0 }?.let { secondsToHm(seconds = it) }
    } else {
        pages?.takeIf { it > 0 }?.toString()
    }

    return HeroEditionUiModel(
        authors = authorString,
        length = length,
        isAudiobook = isAudiobook,
    )
}
