package nl.rhaydus.softcover.feature.widget.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

internal sealed interface RandomPickResolution {
    /** [book] is `null` when the source resolved but holds no unread book. */
    data class Resolved(
        val book: Book?,
        val source: RandomPickSource,
    ) : RandomPickResolution

    /** The configured list is no longer stored on the device, so there is nothing to pick from. */
    data object SourceUnavailable : RandomPickResolution
}
