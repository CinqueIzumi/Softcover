package nl.rhaydus.softcover.core.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class Book(
    val id: Int,
    val canonicalId: Int? = null,
    val title: String,
    val editions: List<BookEdition>,
    val defaultEdition: BookEdition?,
    val rating: Double,
    val headline: String = "",
    val description: String,
    val releaseYear: Int,
    val releaseDate: LocalDate? = null,
    val coverUrl: String,
    val authors: List<Author>,
    val usersCount: Int,
    val ratingsCount: Int,
    val bookSeries: BookSeries?,
    val positionsInSeries: List<Double>,
    val isCompilation: Boolean,
    val tags: List<Tag> = emptyList(),

    val userBook: UserBook?,
    val userBookRead: UserBookRead?,
) {
    val status: BookStatus
        get() = userBook?.status ?: BookStatus.None

    val effectiveReleaseDate: LocalDate?
        get() = currentEdition?.releaseDate ?: defaultEdition?.releaseDate ?: releaseDate

    val isUnreleased: Boolean
        get() {
            val release = effectiveReleaseDate ?: return false

            return release > Clock.System.todayIn(TimeZone.currentSystemDefault())
        }

    val currentEdition: BookEdition?
        get() {
            val userEditionId = userBook?.editionId

            return editions.firstOrNull { it.id == userEditionId }
                ?: editions.firstOrNull { it.owned }
                ?: defaultEdition
                ?: editions.firstOrNull()
        }

    /** Author names for display, falling back to the book's own authors when no edition is known. */
    val authorString: String
        get() = currentEdition?.authorString?.takeIf { it.isNotBlank() }
            ?: authors.joinToString(", ") { it.name }

    val firstPositionInSeries: Double?
        get() = positionsInSeries.firstOrNull()

    val lastPositionInSeries: Double?
        get() = positionsInSeries.lastOrNull()

    val seriesText: String?
        get() {
            val series = bookSeries ?: return null
            val display = positionInSeriesDisplay ?: return series.name

            return "#$display of ${series.amountOfBooks} in ${series.name}"
        }

    val positionInSeriesDisplay: String?
        get() {
            if (positionsInSeries.isEmpty()) return null

            val first = positionsInSeries.first().formatPosition()

            if (positionsInSeries.size == 1) return first

            val last = positionsInSeries.last().formatPosition()

            return "$first-$last"
        }

    private fun Double.formatPosition(): String =
        if (this % 1.0 == 0.0) toInt().toString() else toString()
}
