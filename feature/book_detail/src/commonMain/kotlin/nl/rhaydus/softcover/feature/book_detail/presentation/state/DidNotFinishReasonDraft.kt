package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason

internal data class DidNotFinishReasonDraft(
    val reasons: Set<DidNotFinishReason>,
    val ownWords: String,
)
