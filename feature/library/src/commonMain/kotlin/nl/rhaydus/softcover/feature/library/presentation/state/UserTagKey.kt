package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.TagCategory

/** One of the user's own tags; `UserTag` has no id, so category and name together are the key. */
internal data class UserTagKey(
    val category: TagCategory,
    override val name: String,
) : TagKey
