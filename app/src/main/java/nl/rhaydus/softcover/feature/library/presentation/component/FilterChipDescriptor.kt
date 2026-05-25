package nl.rhaydus.softcover.feature.library.presentation.component

import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue

internal data class FilterChipDescriptor(
    val key: String,
    val label: String,
    val value: LibraryFilterValue,
)
