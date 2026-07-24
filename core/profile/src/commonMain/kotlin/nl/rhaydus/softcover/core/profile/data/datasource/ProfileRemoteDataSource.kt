package nl.rhaydus.softcover.core.profile.data.datasource

import com.apollographql.apollo.ApolloClient
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.GetReadUserBooksForStatsQuery
import nl.rhaydus.softcover.GetReadingActivityDaysQuery
import nl.rhaydus.softcover.GetUserProfileDataQuery
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.network.helper.fetchAllPages
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.profile.data.mapper.toAuthorDemographics
import nl.rhaydus.softcover.core.profile.data.mapper.toBooksByYear
import nl.rhaydus.softcover.core.profile.data.mapper.toGenreBreakdown
import nl.rhaydus.softcover.core.profile.data.mapper.toPagesByMonth
import nl.rhaydus.softcover.core.profile.data.mapper.toPagesByYear
import nl.rhaydus.softcover.core.profile.data.mapper.toRatedCount
import nl.rhaydus.softcover.core.profile.data.mapper.toRatingAverage
import nl.rhaydus.softcover.core.profile.data.mapper.toRatingHalfStarBuckets
import nl.rhaydus.softcover.core.profile.data.mapper.toTotalPages
import nl.rhaydus.softcover.core.profile.data.mapper.toTrackedYears
import nl.rhaydus.softcover.core.profile.data.model.StatsAuthor
import nl.rhaydus.softcover.core.profile.data.model.StatsBook
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

interface ProfileRemoteDataSource {
    suspend fun getUserProfileSnapshot(): UserProfileSnapshot

    fun streamReadingDaysDescending(userId: Int): Flow<LocalDate>
}

private val monthYearLabelFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
}

internal class ProfileRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ProfileRemoteDataSource {
    // region Fetch + snapshot assembly
    override suspend fun getUserProfileSnapshot(): UserProfileSnapshot {
        val currentYear = clock.todayIn(timeZone).year
        val data = apolloClient.safeQuery(query = GetUserProfileDataQuery())
        val me = data
            .me
            .firstOrNull()
            ?: throw Exception("User could not be initialized")

        val statsBooks = fetchAllStatsBooks()

        return UserProfileSnapshot(
            profileImageUrl = me.image?.url.orEmpty(),
            name = me.name.orEmpty(),
            username = me.username?.toString().orEmpty(),
            bio = me.bio.orEmpty(),
            booksRead = me.books_read.aggregate?.count ?: 0,
            totalPagesRead = statsBooks.toTotalPages(),
            averageRating = me.rated_books.aggregate?.avg?.rating ?: 0.0,
            booksByYear = statsBooks.toBooksByYear(currentYear = currentYear),
            pagesByYear = statsBooks.toPagesByYear(currentYear = currentYear),
            pagesByMonth = statsBooks.toPagesByMonth(currentYear = currentYear),
            genres = statsBooks.toGenreBreakdown(),
            trackedYears = statsBooks.toTrackedYears(),
            authorDemographics = statsBooks.toAuthorDemographics(),
            // Scoped to the same read-books dataset as the histogram (not me.rated_books, which is
            // all-status) so the bars sum to totalRatings and RatingBand's bimodal math isn't
            // deflated. The hero "Avg. rating" tile above stays on the server's all-status average.
            ratings = RatingsDistribution(
                average = statsBooks.toRatingAverage(),
                totalRatings = statsBooks.toRatedCount(),
                halfStarBuckets = statsBooks.toRatingHalfStarBuckets(),
            ),
            recentlyLoved = me.recentlyLoved(),
        )
    }

    // Pages through every finished book once per session. Unlike the reading-day stream below,
    // every row is needed to compute accurate aggregates, so this collects eagerly into a list
    // rather than staying a lazily-cancellable Flow.
    private suspend fun fetchAllStatsBooks(): List<StatsBook> =
        fetchAllPages { limit, offset ->
            apolloClient.safeQuery(
                query = GetReadUserBooksForStatsQuery(
                    limit = limit,
                    offset = offset,
                ),
            ).me.firstOrNull()?.user_books.orEmpty()
        }.map { it.toStatsBook() }

    private fun GetReadUserBooksForStatsQuery.Data.Me.User_book.toStatsBook(): StatsBook {
        val journalFinishDate = user_book_read_finished_journal
            .firstOrNull()
            ?.updated_at
            ?.let(::parseDateOrNull)

        val finishDate = last_read_date?.let(::parseDateOrNull) ?: journalFinishDate

        return StatsBook(
            rating = rating,
            finishDate = finishDate,
            pages = edition?.pages ?: book.pages ?: 0,
            genreNames = book.taggable_counts
                .mapNotNull { it.tag?.tag }
                .distinct(),
            authors = book.contributions.mapNotNull { it.author?.toStatsAuthor() },
        )
    }

    private fun GetReadUserBooksForStatsQuery.Data.Me.User_book.Book.Contribution.Author.toStatsAuthor(): StatsAuthor =
        StatsAuthor(
            id = id,
            isBipoc = is_bipoc,
            isLgbtq = is_lgbtq,
            gender = Gender.fromId(gender_id),
        )
    // endregion
    // region me{} response mapping
    private fun GetUserProfileDataQuery.Data.Me.recentlyLoved(): List<LovedBook> = recently_loved.map { loved ->
        LovedBook(
            coverUrl = loved.book.image?.url.orEmpty(),
            title = loved.book.title.orEmpty(),
            author = loved.book.contributions
                .mapNotNull { it.author?.name }
                .joinToString(", "),
            ratingStars = loved.rating ?: 0.0,
            monthLabel = loved.last_read_date.toMonthYearLabel(),
        )
    }

    private fun String?.toMonthYearLabel(): String {
        val date = this
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: return ""

        return monthYearLabelFormat.format(date)
    }
    // endregion
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

    // The live API serves dates as either a bare date ("2026-05-04") or a full ISO timestamp
    // ("2026-05-04T06:20:37.939189+00:00"). A bare date is already a calendar day, so it passes
    // through unchanged; only timestamps carry a time-of-day that needs a zone. Bucket those into
    // the user's local timezone so a day matches the calendar day the user actually acted on -
    // counting in UTC can push a late-evening event onto the next UTC day.
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
