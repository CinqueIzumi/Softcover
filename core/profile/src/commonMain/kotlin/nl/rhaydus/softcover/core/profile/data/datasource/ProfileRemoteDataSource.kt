package nl.rhaydus.softcover.core.profile.data.datasource

import com.apollographql.apollo.ApolloClient
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.GetUserProfileDataQuery
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

interface ProfileRemoteDataSource {
    suspend fun getUserProfileSnapshot(userId: Int): UserProfileSnapshot
}

internal class ProfileRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
    private val timeZone: TimeZone,
) : ProfileRemoteDataSource {
    override suspend fun getUserProfileSnapshot(userId: Int): UserProfileSnapshot {
        val data = apolloClient.safeQuery(query = GetUserProfileDataQuery(userId = userId))

        val me = data
            .me
            .firstOrNull()
            ?: throw Exception("User could not be initialized")

        val activeReadingDates = data.streak_journals
            .mapNotNull { parseDateOrNull(it.action_at) }
            .toSet()

        return UserProfileSnapshot(
            profileImageUrl = me.image?.url ?: "",
            name = me.name ?: "",
            username = me.username?.toString() ?: "",
            bio = me.bio ?: "",
            booksRead = me.books_read.aggregate?.count ?: 0,
            totalPagesRead = me.user_books_pages.sumOf { it.pagesRead() },
            averageRating = me.rated_books.aggregate?.avg?.rating?.toDouble() ?: 0.0,
            activeReadingDates = activeReadingDates,
        )
    }

    private fun GetUserProfileDataQuery.Data.Me.User_books_page.pagesRead(): Int {
        val maxProgress = user_book_reads_aggregate.aggregate?.max?.progress_pages ?: 0

        // A book marked Read should always credit at least its full page count, even if
        // the user never logged that final progress event (or imported it without a log).
        if (status_id == UserBookStatus.READ.code) {
            val fullBookPages = edition?.pages ?: book.pages ?: 0
            return maxOf(
                maxProgress,
                fullBookPages,
            )
        }
        return maxProgress
    }

    // The live API serves action_at as either a bare date ("2026-05-04") or a full
    // ISO timestamp ("2026-05-04T06:20:37.939189+00:00"). A bare date is already a calendar
    // day, so it passes through unchanged; only timestamps carry a time-of-day that needs a
    // zone. Bucket those into the user's local timezone so a reading-day matches the calendar
    // day they actually read on — counting in UTC pushes a late-evening session onto the next
    // UTC day and can manufacture a phantom gap that wrongly breaks the streak.
    private fun parseDateOrNull(value: String): LocalDate? = runCatching {
        if (value.contains('T')) {
            Instant.parse(value).toLocalDateTime(timeZone).date
        } else {
            LocalDate.parse(value)
        }
    }.getOrNull()
}
