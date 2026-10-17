package nl.rhaydus.softcover.feature.reading.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedBookUiModel

internal fun Book.toFeaturedBookUiModel(): FeaturedBookUiModel = FeaturedBookUiModel(
    id = id,
    title = title,
    seriesText = seriesText,
    authors = currentEdition?.authorString,
    isAudiobook = currentEdition?.isAudiobook == true,
)
