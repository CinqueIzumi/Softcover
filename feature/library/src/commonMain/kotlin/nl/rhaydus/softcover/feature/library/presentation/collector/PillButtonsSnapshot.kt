package nl.rhaydus.softcover.feature.library.presentation.collector

internal data class PillButtonsSnapshot(
    val filterActiveByTab: Map<String, Boolean>,
    val bulkActionInProgress: Boolean,
)
