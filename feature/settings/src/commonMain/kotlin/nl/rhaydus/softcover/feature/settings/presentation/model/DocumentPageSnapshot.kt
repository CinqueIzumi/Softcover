package nl.rhaydus.softcover.feature.settings.presentation.model

import kotlinx.collections.immutable.ImmutableList

/** What a page source emits: the page's mapped sections and where they came from. */
internal data class DocumentPageSnapshot(
    val sections: ImmutableList<DocumentSection>,
    val isBundled: Boolean,
    val fetchedAtEpochMillis: Long?,
)
