package nl.rhaydus.softcover.feature.scan.presentation.event

import nl.rhaydus.softcover.core.domain.model.Book

data class BookResolvedEvent(
    val book: Book,
    val editionId: Int?,
) : ScanEvent
