package nl.rhaydus.softcover.core.profile.domain.model

/** One of the reader's own tags and the number of finished books it is attached to. */
data class OwnTagCount(
    val name: String,
    val bookCount: Int,
)
