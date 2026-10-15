package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.UserBookRead

internal data class YourReadsSnapshot(
    val reads: List<UserBookRead>?,
    val status: BookStatus?,
    val dateStyle: DateStyle,
)
