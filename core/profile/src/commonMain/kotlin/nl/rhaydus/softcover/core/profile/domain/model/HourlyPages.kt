package nl.rhaydus.softcover.core.profile.domain.model

/** The pages read in each hour of the local day, midnight first, counted from logged progress updates only. */
data class HourlyPages(val pages: List<Int>)
