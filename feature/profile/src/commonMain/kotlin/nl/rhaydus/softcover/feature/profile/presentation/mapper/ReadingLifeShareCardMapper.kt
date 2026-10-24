package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.share.ReadingLifeGenre
import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import kotlin.math.roundToInt

private const val SHARE_CARD_GENRE_LIMIT = 3
private const val MONTHS_IN_YEAR = 12

/**
 * Maps all-time [ReadingStats], [ReadingHistory] and [UserProfileData] onto the exportable
 * [ReadingLifeShareCardUiModel] the share sheet renders. [ReadingLifeShareCardUiModel] deliberately
 * carries no `core:profile` dependency, so this mapping — not a shared use case — is where they meet.
 */
internal fun ReadingStats.toReadingLifeShareCardUiModel(
    history: ReadingHistory,
    profile: UserProfileData,
): ReadingLifeShareCardUiModel =
    ReadingLifeShareCardUiModel(
        readerName = profile.name,
        avatarUrl = profile.profileImageUrl.takeIf { it.isNotBlank() },
        readerInitials = profile.name.toInitials(),
        totalPagesRead = formatGroupedNumber(totalPages),
        totalBooksLine = "across $booksRead ${if (booksRead == 1) "book" else "books"}",
        // Already ranked highest-first by the mapper, so the card's leading row is the top genre.
        topGenres = genres.slices.take(SHARE_CARD_GENRE_LIMIT).map { slice ->
            ReadingLifeGenre(
                name = slice.name,
                percentLabel = "${(slice.fraction * PERCENTAGE_MULTIPLIER).roundToInt()}%",
            )
        }.toImmutableList(),
        pagesByMonth = history.currentYearMonthlyPages().toImmutableList(),
        averageRating = ratings.average,
        averageRatingText = formatDecimalNumber(
            value = ratings.average,
            fractionDigits = 1,
        ),
        dayStreak = profile.readingStreak.toString(),
        trackedYears = history.trackedYears.toString(),
    )

private fun String.toInitials(): String {
    val letters = trim()
        .split(" ")
        .mapNotNull { word -> word.firstOrNull()?.uppercaseChar() }

    return when {
        letters.isEmpty() -> "?"
        letters.size == 1 -> letters.first().toString()
        else -> "${letters.first()}${letters.last()}"
    }
}

private fun ReadingHistory.currentYearMonthlyPages(): List<Int> {
    val year = pagesByMonth.maxOfOrNull { it.year } ?: return List(MONTHS_IN_YEAR) { 0 }
    val countByMonth = pagesByMonth.filter { it.year == year }.associate { it.month to it.count }

    return (1..MONTHS_IN_YEAR).map { month -> countByMonth[month] ?: 0 }
}
