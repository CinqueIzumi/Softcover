package nl.rhaydus.softcover.feature.book_detail.presentation.component

import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag

/** One category's tags in the [TagEditorBottomSheet] collection. */
internal data class TagGroup(
    val category: TagCategory,
    val tags: List<UserTag>,
)
