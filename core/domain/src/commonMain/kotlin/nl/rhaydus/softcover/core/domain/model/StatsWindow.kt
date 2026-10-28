package nl.rhaydus.softcover.core.domain.model

/** The trailing spans a [StatsScope.Rolling] scope can cover, each ending today. */
enum class StatsWindow {
    LAST_30_DAYS,
    LAST_6_MONTHS,
    LAST_12_MONTHS,
}
