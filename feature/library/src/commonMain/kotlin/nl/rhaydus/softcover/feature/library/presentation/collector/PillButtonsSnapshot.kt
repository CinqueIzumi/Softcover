package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.domain.model.UserBookStatus

internal data class PillButtonsSnapshot(
    val filterActiveByTab: Map<String, Boolean>,
    val bulkActionInProgress: Boolean,
    val currentShelf: UserBookStatus?,
)
