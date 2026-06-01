package nl.rhaydus.softcover.core.domain.model

/**
 * A tag the current user has personally attached to a book — a `taggings` row, distinct from the
 * editorial [Tag] that backs the read-only popularity-ranked display. A user tag is identified by
 * its [category] + [name] pair: the upsert API works purely on those strings (and creates the tag
 * server-side when the name is new), and the save response carries no tag id, so there is nothing
 * stable to key on but the pair. [spoiler] marks the tag as hidden from other readers.
 */
data class UserTag(
    val name: String,
    val category: TagCategory,
    val count: Int = 0,
    val spoiler: Boolean = false,
)
