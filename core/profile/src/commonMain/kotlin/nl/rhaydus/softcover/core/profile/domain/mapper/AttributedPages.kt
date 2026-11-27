package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate

/** Pages credited to [bookId] on [date]; [kind] tells a logged update from an unlogged remainder. */
internal data class AttributedPages(
    val date: LocalDate,
    val bookId: Int,
    val pages: Int,
    val kind: AttributedPagesKind,
)
