package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.domain.model.TagCategory

/**
 * The tag editor's four user-editable categories, in display order — the single copy
 * `TagEditorChipModelsCollector` (which maps this list to the category picker chips) and
 * `TagEditorBottomSheet` (which folds any tag outside this set into a trailing "Other" group when
 * grouping the book's collected tags) both share, so the two cannot drift apart.
 */
internal val EDITABLE_CATEGORIES: List<TagCategory> = listOf(
    TagCategory.GENRE,
    TagCategory.MOOD,
    TagCategory.TAG,
    TagCategory.CONTENT_WARNING,
)
