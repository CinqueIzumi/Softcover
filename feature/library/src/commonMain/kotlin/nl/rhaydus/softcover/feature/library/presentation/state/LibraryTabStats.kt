package nl.rhaydus.softcover.feature.library.presentation.state

/**
 * Precomputed header stats for a library tab. [itemCount] is the number of rendered entries —
 * books for built-in shelves, editions for custom lists — so the name stays accurate for both.
 */
internal data class LibraryTabStats(
    val itemCount: Int,
    val totalPages: Int,
)
