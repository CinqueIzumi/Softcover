package nl.rhaydus.softcover.feature.profile.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetUserProfileDataQuery
import nl.rhaydus.softcover.core.data.network.helper.safeQuery
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileSnapshot
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface ProfileRemoteDataSource {
    suspend fun getUserProfileSnapshot(userId: Int): UserProfileSnapshot
}

class ProfileRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
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
            return maxOf(maxProgress, fullBookPages)
        }
        return maxProgress
    }

    // The live API serves action_at as either a bare date ("2026-05-04") or a full
    // ISO timestamp ("2026-05-04T06:20:37.939189+00:00"). Convert timestamps to UTC
    // before extracting the date so non-UTC offsets don't shift entries to the wrong day.
    private fun parseDateOrNull(value: String): LocalDate? = runCatching {
        if (value.contains('T')) {
            OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDate()
        } else {
            LocalDate.parse(value)
        }
    }.getOrNull()
}
