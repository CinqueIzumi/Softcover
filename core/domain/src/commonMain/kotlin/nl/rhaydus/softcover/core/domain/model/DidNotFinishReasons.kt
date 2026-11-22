package nl.rhaydus.softcover.core.domain.model

data class DidNotFinishReasons(
    val bookId: Int,
    val reasons: Set<DidNotFinishReason> = emptySet(),
    val ownWords: String? = null,
) {
    val isEmpty: Boolean
        get() = reasons.isEmpty() && ownWords == null
}
