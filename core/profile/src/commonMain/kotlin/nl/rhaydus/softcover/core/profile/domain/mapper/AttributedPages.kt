package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate

/** Pages credited to [bookId] on [date]; [isRemainder] marks the unlogged final stretch added on the finish date. */
internal data class AttributedPages(
    val date: LocalDate,
    val bookId: Int,
    val pages: Int,
    val isRemainder: Boolean,
)
