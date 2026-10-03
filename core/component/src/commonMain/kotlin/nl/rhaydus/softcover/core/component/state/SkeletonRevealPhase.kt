package nl.rhaydus.softcover.core.component.state

/** What a skeleton-masked leaf draws: nothing, the shimmer, or its real content. */
internal enum class SkeletonRevealPhase {
    Hidden,
    Shimmer,
    Content,
}
