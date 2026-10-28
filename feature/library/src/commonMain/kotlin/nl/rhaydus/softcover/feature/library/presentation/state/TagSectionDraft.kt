package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.TagCategory

/**
 * One tag source's search state inside the Filter sheet. A non-blank [search] narrows the unchosen tags and
 * shows the category chips; a null [category] is "All".
 */
internal data class TagSectionDraft(
    val search: String = "",
    val category: TagCategory? = null,
)
