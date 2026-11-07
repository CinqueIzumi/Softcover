package nl.rhaydus.softcover.core.presentation.navigation

import nl.rhaydus.softcover.core.domain.model.TagCategory

/** A request to show the Library's Read shelf filtered to one of the reader's own tags. */
data class LibraryTagFilterRequest(
    val category: TagCategory,
    val name: String,
)
