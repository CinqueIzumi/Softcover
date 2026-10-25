package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.TagCategory

/** A tag present on a tab, with the number of that tab's books carrying it. */
internal data class LibraryTagOption<K : TagKey>(
    val key: K,
    val category: TagCategory,
    val count: Int,
)
