package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.ReadingFormat

/** A book counts once, under the reader's own edition, since a per-read edition is not synced. */
data class FormatSplit(
    val counts: Map<ReadingFormat, Int> = emptyMap(),
    val unknownCount: Int = 0,
) {
    val total: Int get() = counts.values.sum() + unknownCount
}
