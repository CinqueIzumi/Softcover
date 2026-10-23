package nl.rhaydus.softcover.feature.widget.domain.model

internal sealed interface RandomPickResolution {
    /** [pick] is `null` when the source resolved but holds no unread book. */
    data class Resolved(
        val pick: RandomPickBook?,
        val source: RandomPickSource,
    ) : RandomPickResolution

    /** The configured list is no longer stored on the device, so there is nothing to pick from. */
    data object SourceUnavailable : RandomPickResolution
}
