package nl.rhaydus.softcover.feature.profile.presentation.screen

/**
 * The two series [YearColumnHistorySection]'s toggle switches the year-column chart between —
 * [nl.rhaydus.softcover.core.profile.domain.model.ReadingLife.booksByYear] or `.pagesByYear`.
 */
internal enum class YearMetric(val label: String) {
    BOOKS(label = "Books"),
    PAGES(label = "Pages"),
}
