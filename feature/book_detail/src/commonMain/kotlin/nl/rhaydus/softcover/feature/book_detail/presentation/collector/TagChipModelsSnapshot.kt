package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.UserTag

/** The state fields the book page's tag chip surfaces (read-only, community, and the tag editor) are derived from. */
internal data class TagChipModelsSnapshot(
    val userTags: List<UserTag>,
    val communityTags: List<Tag>,
    val revealedTagKeys: Set<String>,
)
