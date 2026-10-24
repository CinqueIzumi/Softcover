package nl.rhaydus.softcover.feature.profile.presentation.state

/**
 * The two series the year-column chart switches between —
 * [nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory.booksByYear] or `.pagesByYear`.
 */
internal enum class YearMetric(val label: String) {
    BOOKS(label = "Books"),
    PAGES(label = "Pages"),
}
