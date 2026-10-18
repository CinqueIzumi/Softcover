package nl.rhaydus.softcover.core.book.data.mapper

import nl.rhaydus.softcover.core.domain.model.UpcomingWindow

internal fun UpcomingWindow.toMonths(): Int = when (this) {
    UpcomingWindow.Month -> 1
    UpcomingWindow.ThreeMonths -> 3
    UpcomingWindow.SixMonths -> 6
    UpcomingWindow.Year -> 12
}

/** Prefixed so an upcoming list shares the trending tables without colliding with a trending period's key. */
internal fun UpcomingWindow.toStorageKey(): String = "upcoming_${name.lowercase()}"
