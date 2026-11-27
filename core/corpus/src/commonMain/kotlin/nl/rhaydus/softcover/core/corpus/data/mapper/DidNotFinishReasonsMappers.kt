package nl.rhaydus.softcover.core.corpus.data.mapper

import nl.rhaydus.softcover.core.database.model.DidNotFinishReasonsEntity
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons

internal fun DidNotFinishReasonsEntity.toDomain(): DidNotFinishReasons = DidNotFinishReasons(
    bookId = bookId,
    reasons = buildSet {
        if (notTheRightTime) add(DidNotFinishReason.NotTheRightTime)
        if (notMyKindOfWriting) add(DidNotFinishReason.NotMyKindOfWriting)
        if (tooSlow) add(DidNotFinishReason.TooSlow)
        if (lostInterest) add(DidNotFinishReason.LostInterest)
        if (unwantedContent) add(DidNotFinishReason.UnwantedContent)
    },
    ownWords = ownWords,
)

internal fun DidNotFinishReasons.toEntity(updatedAt: Long): DidNotFinishReasonsEntity = DidNotFinishReasonsEntity(
    bookId = bookId,
    notTheRightTime = DidNotFinishReason.NotTheRightTime in reasons,
    notMyKindOfWriting = DidNotFinishReason.NotMyKindOfWriting in reasons,
    tooSlow = DidNotFinishReason.TooSlow in reasons,
    lostInterest = DidNotFinishReason.LostInterest in reasons,
    unwantedContent = DidNotFinishReason.UnwantedContent in reasons,
    ownWords = ownWords,
    updatedAt = updatedAt,
)
