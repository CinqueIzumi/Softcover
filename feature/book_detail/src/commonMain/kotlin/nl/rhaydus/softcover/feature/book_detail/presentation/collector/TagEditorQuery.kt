package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag

internal data class TagEditorQuery(
    val input: String,
    val category: TagCategory,
    val applied: List<UserTag>,
)
