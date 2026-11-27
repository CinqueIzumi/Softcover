package nl.rhaydus.softcover.feature.book_detail.presentation

import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.feature.book_detail.presentation.state.DidNotFinishReasonDraft

internal fun buildDidNotFinishReasonDraft(
    reasons: Set<DidNotFinishReason> = emptySet(),
    ownWords: String = "",
): DidNotFinishReasonDraft = DidNotFinishReasonDraft(
    reasons = reasons,
    ownWords = ownWords,
)
