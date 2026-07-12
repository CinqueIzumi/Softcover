package nl.rhaydus.softcover.feature.explore.data.mapper

import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesEntity
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook

internal fun DismissedContinueSeriesBookEntity.toDomain(): DismissedSeriesBook = DismissedSeriesBook(
    bookId = bookId,
    title = bookTitle,
    coverUrl = coverUrl,
    authorText = authorText,
    seriesName = seriesName,
)

internal fun DismissedContinueSeriesEntity.toDomain(): DismissedSeries = DismissedSeries(
    seriesId = seriesId,
    seriesName = seriesName,
    coverUrl = coverUrl,
)
