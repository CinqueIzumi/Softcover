package nl.rhaydus.softcover.core.book.data.mapper

import kotlinx.datetime.DatePeriod
import nl.rhaydus.softcover.core.domain.model.RecentWindow

internal fun RecentWindow.toDatePeriod(): DatePeriod = when (this) {
    RecentWindow.Week -> DatePeriod(days = 7)
    RecentWindow.Month -> DatePeriod(months = 1)
    RecentWindow.ThreeMonths -> DatePeriod(months = 3)
    RecentWindow.SixMonths -> DatePeriod(months = 6)
}

/** Prefixed so a recent list shares the ranked_books table without colliding with a trending or upcoming key. */
internal fun RecentWindow.toStorageKey(): String = "recent_${name.lowercase()}"
