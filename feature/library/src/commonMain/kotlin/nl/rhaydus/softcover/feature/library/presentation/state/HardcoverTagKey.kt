package nl.rhaydus.softcover.feature.library.presentation.state

/** A Hardcover community tag, keyed by its id. Never merged with a [UserTagKey] of the same name. */
internal data class HardcoverTagKey(
    val id: Int,
    override val name: String,
) : TagKey
