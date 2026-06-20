package nl.rhaydus.softcover.core.database.model

/**
 * Projection of an edition's cover-image bookkeeping: the persisted local file [localImagePath] and
 * the URL it was downloaded from ([localImageUrl]). A local cover is valid only while [localImageUrl]
 * still matches the edition's current `url`; once they diverge the file is stale and must be evicted.
 */
data class EditionImageRef(
    val id: Int,
    val localImagePath: String?,
    val localImageUrl: String?,
)
