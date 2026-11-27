package nl.rhaydus.softcover.core.uibinding.didnotfinish

import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason

fun DidNotFinishReason.toLabel(): String = when (this) {
    DidNotFinishReason.NotTheRightTime -> "Not the right time"
    DidNotFinishReason.NotMyKindOfWriting -> "Not my kind of writing"
    DidNotFinishReason.TooSlow -> "Too slow"
    DidNotFinishReason.LostInterest -> "Lost interest"
    DidNotFinishReason.UnwantedContent -> "Content I didn't want to read"
}
