package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book

internal data class ResolvedScan(
    val book: Book,
    val editionId: Int?,
)
