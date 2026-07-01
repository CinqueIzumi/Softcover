package nl.rhaydus.softcover.core.profile.data.datasource

import com.apollographql.apollo.ApolloClient
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.GetReadingActivityDaysQuery
import nl.rhaydus.softcover.GetUserProfileDataQuery
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

interface ProfileRemoteDataSource {
    suspend fun getUserProfileSnapshot(): UserProfileSnapshot

    fun streamReadingDaysDescending(userId: Int): Flow<LocalDate>
}

internal class ProfileRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
    private val timeZone: TimeZone,
) : ProfileRemoteDataSource {
    override suspend fun getUserProfileSnapshot(): UserProfileSnapshot {
        val data = apolloClient.safeQuery(query = GetUserProfileDataQuery())

        val me = data
            .me
            .firstOrNull()
            ?: throw Exception("User could not be initialized")

        return UserProfileSnapshot(
            profileImageUrl = me.image?.url ?: "",
            name = me.name ?: "",
            username = me.username?.toString() ?: "",
            bio = me.bio ?: "",
            booksRead = me.books_read.aggregate?.count ?: 0,
            totalPagesRead = me.user_books_pages.sumOf { it.pagesRead() },
            averageRating = me.rated_books.aggregate?.avg?.rating ?: 0.0,
        )
    }

    // Cold: pages GetReadingActivityDaysQuery (ordered action_at desc) lazily, one page at a
    // time, only while a collector keeps requesting values. A collector that stops early (e.g.
    // ProfileRepository's takeWhile) cancels this flow before the next page is ever fetched, so
    // a short streak or a narrow window never pays for the account's full history.
    override fun streamReadingDaysDescending(userId: Int): Flow<LocalDate> = flow {
        var offset = 0
        var pages = 0

        while (pages < MAX_PAGES) {
            val data = apolloClient.safeQuery(
                query = GetReadingActivityDaysQuery(
                    userId = userId,
                    limit = PAGE_SIZE,
                    offset = offset,
                ),
            )
            val rawCount = data.reading_journals.size

            data.reading_journals.forEach { row ->
                parseDateOrNull(row.action_at)?.let { emit(it) }
            }

            offset += rawCount
            pages++

            // No rows at all: history is exhausted, nothing left to page through.
            if (rawCount == 0) break
        }
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

    private companion object {
        // Matches the server's response cap; advancing offset by the page's actual row count
        // keeps paging correct even if that cap ever changes.
        const val PAGE_SIZE = 100

        // Runaway guard so a pathological account can never turn this into an unbounded loop.
        const val MAX_PAGES = 100
    }
}
