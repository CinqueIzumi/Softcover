package nl.rhaydus.softcover.feature.explore.presentation.state

import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesDismissalKind

internal data class PendingContinueSeriesDismissal(
    val bookId: Int,
    val kind: ContinueSeriesDismissalKind,
)
