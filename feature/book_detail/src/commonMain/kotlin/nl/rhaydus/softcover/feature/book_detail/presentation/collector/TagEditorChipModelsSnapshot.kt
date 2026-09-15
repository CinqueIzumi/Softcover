package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag

/** The state fields the tag editor's category picker and suggestion cloud chips derive from. */
internal data class TagEditorChipModelsSnapshot(
    val selectedCategory: TagCategory,
    val suggestions: List<UserTag>,
)
