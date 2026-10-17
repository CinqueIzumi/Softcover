package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.DateStyle

internal data class CalloutInputs(
    val book: Book?,
    val dateStyle: DateStyle,
    val scannedEditionId: Int?,
    val scannedEditionBannerDismissed: Boolean,
    val isUpdatingScannedEdition: Boolean,
)
