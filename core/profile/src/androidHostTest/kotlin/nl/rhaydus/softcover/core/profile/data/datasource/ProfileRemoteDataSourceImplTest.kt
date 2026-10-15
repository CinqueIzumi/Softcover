package nl.rhaydus.softcover.core.profile.data.datasource

import com.apollographql.apollo.ApolloClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import nl.rhaydus.softcover.GetReadUserBooksForStatsQuery
import nl.rhaydus.softcover.GetReadingActivityDaysQuery
import nl.rhaydus.softcover.GetUserProfileDataQuery
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

private const val STATS_PAGE_SIZE = 100
private const val STATS_MAX_PAGES = 100

class ProfileRemoteDataSourceImplTest {
    // Fixed "now" so clock.todayIn(timeZone).year is deterministic across tests: 2026.
    private val fixedClock: Clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-05-04T12:00:00Z")
    }

    private lateinit var apolloClient: ApolloClient
    private lateinit var dataSource: ProfileRemoteDataSourceImpl

    companion object {
        private const val APOLLO_EXTENSIONS = "nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt"

        // Static-mocking the Apollo extensions is the expensive step, so it happens once per class.
        // Every test stubs against its own fresh ApolloClient mock, so no stub leaks between tests.
        @JvmStatic
        @BeforeAll
        fun mockApolloExtensions() {
            mockkStatic(APOLLO_EXTENSIONS)
        }

        @JvmStatic
        @AfterAll
        fun unmockApolloExtensions() {
            unmockkStatic(APOLLO_EXTENSIONS)
        }
    }

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        dataSource = ProfileRemoteDataSourceImpl(
            apolloClient = apolloClient,
            clock = fixedClock,
            timeZone = TimeZone.UTC,
        )
    }

    // region me{} builders
    private fun booksReadAgg(count: Int?): GetUserProfileDataQuery.Data.Me.Books_read =
        GetUserProfileDataQuery.Data.Me.Books_read(
            __typename = "user_books_aggregate",
            aggregate = count?.let {
                GetUserProfileDataQuery.Data.Me.Books_read.Aggregate(
                    __typename = "aggregate",
                    count = it,
                )
            },
        )

    private fun ratedBooksAgg(
        avgRating: Double?,
        hasAggregate: Boolean = true,
    ): GetUserProfileDataQuery.Data.Me.Rated_books = GetUserProfileDataQuery.Data.Me.Rated_books(
        __typename = "user_books_aggregate",
        aggregate = if (hasAggregate.not()) {
            null
        } else {
            GetUserProfileDataQuery.Data.Me.Rated_books.Aggregate(
                __typename = "aggregate",
                count = 0,
                avg = GetUserProfileDataQuery.Data.Me.Rated_books.Aggregate.Avg(
                    __typename = "avg_fields",
                    rating = avgRating,
                ),
            )
        },
    )

    private fun lovedEntry(
        rating: Double?,
        lastReadDate: String?,
        title: String?,
        imageUrl: String?,
        authorNames: List<String?>,
        edition: GetUserProfileDataQuery.Data.Me.Recently_loved.Edition? = null,
    ): GetUserProfileDataQuery.Data.Me.Recently_loved = GetUserProfileDataQuery.Data.Me.Recently_loved(
        __typename = "user_books",
        id = 1,
        rating = rating,
        last_read_date = lastReadDate,
        edition = edition,
        book = GetUserProfileDataQuery.Data.Me.Recently_loved.Book(
            __typename = "books",
            title = title,
            image = imageUrl?.let {
                GetUserProfileDataQuery.Data.Me.Recently_loved.Book.Image(
                    __typename = "images",
                    url = it,
                )
            },
            contributions = authorNames.map { authorName ->
                GetUserProfileDataQuery.Data.Me.Recently_loved.Book.Contribution(
                    __typename = "contributions",
                    author = authorName?.let {
                        GetUserProfileDataQuery.Data.Me.Recently_loved.Book.Contribution.Author(
                            __typename = "authors",
                            name = it,
                            id = 1,
                        )
                    },
                )
            },
            id = 1,
        ),
    )

    private fun lovedEdition(imageUrl: String?): GetUserProfileDataQuery.Data.Me.Recently_loved.Edition =
        GetUserProfileDataQuery.Data.Me.Recently_loved.Edition(
            __typename = "editions",
            image = imageUrl?.let {
                GetUserProfileDataQuery.Data.Me.Recently_loved.Edition.Image(
                    __typename = "images",
                    url = it,
                )
            },
            id = 2,
        )

    private fun me(
        bio: String? = "A bio",
        image: GetUserProfileDataQuery.Data.Me.Image? = GetUserProfileDataQuery.Data.Me.Image(
            __typename = "images",
            url = "https://example.com/avatar.jpg",
        ),
        name: String? = "John Doe",
        username: Any? = "johndoe",
        booksRead: GetUserProfileDataQuery.Data.Me.Books_read = booksReadAgg(count = 42),
        ratedBooks: GetUserProfileDataQuery.Data.Me.Rated_books = ratedBooksAgg(avgRating = 3.75),
        recentlyLoved: List<GetUserProfileDataQuery.Data.Me.Recently_loved> = emptyList(),
    ): GetUserProfileDataQuery.Data.Me = GetUserProfileDataQuery.Data.Me(
        __typename = "users",
        bio = bio,
        image = image,
        name = name,
        username = username,
        books_read = booksRead,
        rated_books = ratedBooks,
        recently_loved = recentlyLoved,
        id = 1,
    )

    private fun mockProfileQuery(vararg entries: GetUserProfileDataQuery.Data.Me) {
        coEvery {
            apolloClient.safeQuery(query = ofType<GetUserProfileDataQuery>())
        } returns GetUserProfileDataQuery.Data(me = entries.toList())
    }
    // endregion
    // region stats-book builders
    private fun contribution(
        id: Int = 1,
        isBipoc: Boolean? = null,
        isLgbtq: Boolean? = null,
        genderId: Int? = null,
    ): GetReadUserBooksForStatsQuery.Data.Me.User_book.Book.Contribution =
        GetReadUserBooksForStatsQuery.Data.Me.User_book.Book.Contribution(
            __typename = "contributions",
            author = GetReadUserBooksForStatsQuery.Data.Me.User_book.Book.Contribution.Author(
                __typename = "authors",
                id = id,
                is_bipoc = isBipoc,
                is_lgbtq = isLgbtq,
                gender_id = genderId,
            ),
        )

    private fun nullAuthorContribution(): GetReadUserBooksForStatsQuery.Data.Me.User_book.Book.Contribution =
        GetReadUserBooksForStatsQuery.Data.Me.User_book.Book.Contribution(
            __typename = "contributions",
            author = null,
        )

    private fun statsUserBook(
        rating: Double? = null,
        lastReadDate: String? = null,
        finishedJournalUpdatedAt: String? = null,
        editionPages: Int? = null,
        bookPages: Int? = null,
        genreTags: List<String?> = emptyList(),
        contributions: List<GetReadUserBooksForStatsQuery.Data.Me.User_book.Book.Contribution> = emptyList(),
    ): GetReadUserBooksForStatsQuery.Data.Me.User_book {
        val journal = finishedJournalUpdatedAt?.let {
            listOf(
                GetReadUserBooksForStatsQuery.Data.Me.User_book.User_book_read_finished_journal(
                    __typename = "reading_journals",
                    event = "user_book_read_finished",
                    updated_at = it,
                ),
            )
        }.orEmpty()

        val edition = editionPages?.let {
            GetReadUserBooksForStatsQuery.Data.Me.User_book.Edition(
                __typename = "editions",
                pages = it,
                id = 1,
            )
        }

        val taggableCounts = genreTags.map { tagName ->
            GetReadUserBooksForStatsQuery.Data.Me.User_book.Book.Taggable_count(
                __typename = "taggable_counts",
                tag = tagName?.let {
                    GetReadUserBooksForStatsQuery.Data.Me.User_book.Book.Taggable_count.Tag(
                        __typename = "tags",
                        tag = it,
                    )
                },
            )
        }

        return GetReadUserBooksForStatsQuery.Data.Me.User_book(
            __typename = "user_books",
            rating = rating,
            last_read_date = lastReadDate,
            user_book_read_finished_journal = journal,
            edition = edition,
            book = GetReadUserBooksForStatsQuery.Data.Me.User_book.Book(
                __typename = "books",
                pages = bookPages,
                taggable_counts = taggableCounts,
                contributions = contributions,
                id = 1,
            ),
            id = 1,
        )
    }

    private fun statsPage(vararg books: GetReadUserBooksForStatsQuery.Data.Me.User_book): GetReadUserBooksForStatsQuery.Data =
        GetReadUserBooksForStatsQuery.Data(
            me = listOf(
                GetReadUserBooksForStatsQuery.Data.Me(
                    __typename = "users",
                    user_books = books.toList(),
                    id = 1,
                ),
            ),
        )

    private fun mockEmptyStatsQuery() {
        coEvery {
            apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
        } returns statsPage()
    }
    // endregion
    @Nested
    inner class GetUserProfileSnapshot {
        @Test
        fun `maps profile fields from the me query`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(
                me(
                    bio = "A bio",
                    name = "John Doe",
                    username = "johndoe",
                    booksRead = booksReadAgg(count = 42),
                    ratedBooks = ratedBooksAgg(avgRating = 3.75),
                ),
            )
            mockEmptyStatsQuery()

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            // pagesByMonth is always zero-filled for all 12 months of the current year (2026),
            // never empty, even when there are no stats books at all.
            result shouldBe UserProfileSnapshot(
                profileImageUrl = "https://example.com/avatar.jpg",
                name = "John Doe",
                username = "johndoe",
                bio = "A bio",
                booksRead = 42,
                totalPagesRead = 0,
                averageRating = 3.75,
                pagesByMonth = (1..12).map {
                    MonthCount(
                        year = 2026,
                        month = it,
                        count = 0,
                    )
                },
                booksByYear = (2019..2026).map {
                    YearCount(
                        year = it,
                        count = 0,
                    )
                },
                pagesByYear = (2019..2026).map {
                    YearCount(
                        year = it,
                        count = 0,
                    )
                },
                ratings = RatingsDistribution(
                    average = 0.0,
                    totalRatings = 0,
                    halfStarBuckets = List(9) { 0 },
                ),
                trackedYears = 0,
            )
        }

        @Test
        fun `maps null name, bio, username, image, image url and aggregates to empty and zero fallbacks`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(
                me(
                    bio = null,
                    image = GetUserProfileDataQuery.Data.Me.Image(
                        __typename = "images",
                        url = null,
                    ),
                    name = null,
                    username = null,
                    booksRead = booksReadAgg(count = null),
                    ratedBooks = ratedBooksAgg(
                        avgRating = null,
                        hasAggregate = false,
                    ),
                ),
            )
            mockEmptyStatsQuery()

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.name shouldBe ""
            result.username shouldBe ""
            result.bio shouldBe ""
            result.profileImageUrl shouldBe ""
            result.booksRead shouldBe 0
            result.averageRating shouldBe 0.0
        }

        @Test
        fun `maps a null image to an empty profileImageUrl`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(me(image = null))
            mockEmptyStatsQuery()

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.profileImageUrl shouldBe ""
        }

        @Test
        fun `throws when me list is empty`() = runTest {
            // ----- Arrange -----
            mockProfileQuery()

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.getUserProfileSnapshot()
            }
        }

        @Test
        fun `throws when the stats query fails`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("stats query failed")

            mockProfileQuery(me())
            coEvery {
                apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
            } throws expectedError

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                dataSource.getUserProfileSnapshot()
            }
        }

        @Test
        fun `derives totalPagesRead, booksByYear, pagesByYear, pagesByMonth, genres and ratings from the paginated stats books`() =
            runTest {
                // ----- Arrange -----
                // Book A: 2026-03-10, 300 pages (edition), genres Fantasy + Sci-Fi, rating 4.5
                // Book B: 2025-12-01, 200 pages (book fallback), genre Mystery, rating null
                // Book C: no resolvable date, 150 pages, no genres, rating 3.0
                mockProfileQuery(me())
                coEvery {
                    apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
                } returns statsPage(
                    statsUserBook(
                        rating = 4.5,
                        lastReadDate = "2026-03-10",
                        editionPages = 300,
                        genreTags = listOf("Fantasy", "Sci-Fi"),
                    ),
                    statsUserBook(
                        rating = null,
                        lastReadDate = "2025-12-01",
                        bookPages = 200,
                        genreTags = listOf("Mystery"),
                    ),
                    statsUserBook(
                        rating = 3.0,
                        bookPages = 150,
                    ),
                )

                // ----- Act -----
                val result = dataSource.getUserProfileSnapshot()

                // ----- Assert -----
                result.totalPagesRead shouldBe 650
                result.booksByYear shouldBe (2019..2026).map { year ->
                    YearCount(
                        year = year,
                        count = if (year == 2025 || year == 2026) 1 else 0,
                    )
                }
                result.pagesByYear shouldBe (2019..2026).map { year ->
                    YearCount(
                        year = year,
                        count = when (year) {
                            2025 -> 200
                            2026 -> 300
                            else -> 0
                        },
                    )
                }
                result.pagesByMonth.first { it.month == 3 } shouldBe MonthCount(
                    year = 2026,
                    month = 3,
                    count = 300,
                )
                result.pagesByMonth.size shouldBe 12
                result.genres.slices.map { it.name to it.count }.toSet() shouldBe setOf(
                    "Fantasy" to 1,
                    "Sci-Fi" to 1,
                    "Mystery" to 1,
                )
                result.ratings shouldBe RatingsDistribution(
                    average = 3.75,
                    totalRatings = 2,
                    halfStarBuckets = listOf(0, 0, 0, 0, 1, 0, 0, 1, 0),
                )
                // Book A (2026) and Book B (2025) are distinct finish-years; Book C has no
                // resolvable date and does not contribute.
                result.trackedYears shouldBe 2
            }
    }

    @Nested
    inner class Pagination {
        @Test
        fun `a single partial page makes exactly one network call`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(me())
            coEvery {
                apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
            } returns statsPage(
                statsUserBook(bookPages = 100),
                statsUserBook(bookPages = 50),
            )

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe 150
            coVerify(exactly = 1) { apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>()) }
        }

        @Test
        fun `a full page requests the next page, which stops once it comes back partial`() = runTest {
            // ----- Arrange -----
            val fullPage = statsPage(*List(STATS_PAGE_SIZE) { statsUserBook(bookPages = 1) }.toTypedArray())
            val partialPage = statsPage(*List(5) { statsUserBook(bookPages = 1) }.toTypedArray())

            mockProfileQuery(me())
            coEvery {
                apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
            } returnsMany listOf(fullPage, partialPage)

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe STATS_PAGE_SIZE + 5
            coVerify(exactly = 2) { apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>()) }
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = GetReadUserBooksForStatsQuery(
                        limit = STATS_PAGE_SIZE,
                        offset = STATS_PAGE_SIZE,
                    ),
                )
            }
        }

        @Test
        fun `stops at STATS_MAX_PAGES even when every page comes back full`() = runTest {
            // ----- Arrange -----
            val fullPage = statsPage(*List(STATS_PAGE_SIZE) { statsUserBook(bookPages = 1) }.toTypedArray())

            mockProfileQuery(me())
            coEvery {
                apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
            } returns fullPage

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe STATS_PAGE_SIZE * STATS_MAX_PAGES
            coVerify(exactly = STATS_MAX_PAGES) { apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>()) }
        }
    }

    @Nested
    inner class StatsBookFieldMapping {
        @Test
        fun `last_read_date takes priority over the finished-journal fallback when both are present`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(me())
            coEvery {
                apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
            } returns statsPage(
                statsUserBook(
                    lastReadDate = "2025-01-05",
                    finishedJournalUpdatedAt = "2026-02-20",
                    bookPages = 100,
                ),
            )

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.booksByYear shouldBe (2019..2026).map { year ->
                YearCount(
                    year = year,
                    count = if (year == 2025) 1 else 0,
                )
            }
        }

        @Test
        fun `falls back to the finished journal's updated_at when last_read_date is null`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(me())
            coEvery {
                apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
            } returns statsPage(
                statsUserBook(
                    lastReadDate = null,
                    finishedJournalUpdatedAt = "2026-02-20",
                    bookPages = 100,
                ),
            )

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.booksByYear shouldBe (2019..2026).map { year ->
                YearCount(
                    year = year,
                    count = if (year == 2026) 1 else 0,
                )
            }
        }

        @Test
        fun `uses edition pages over book pages, falls back to book pages and contributes 0 when neither is present`() =
            runTest {
                // ----- Arrange -----
                mockProfileQuery(me())
                coEvery {
                    apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
                } returns statsPage(
                    statsUserBook(
                        editionPages = 300,
                        bookPages = 999,
                    ),
                    statsUserBook(
                        editionPages = null,
                        bookPages = 280,
                    ),
                    statsUserBook(
                        editionPages = null,
                        bookPages = null,
                    ),
                )

                // ----- Act -----
                val result = dataSource.getUserProfileSnapshot()

                // ----- Assert -----
                result.totalPagesRead shouldBe 580
            }

        @Test
        fun `genre tags are deduplicated and a null tag reference is skipped`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(me())
            coEvery {
                apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
            } returns statsPage(
                statsUserBook(
                    lastReadDate = "2026-01-01",
                    bookPages = 100,
                    genreTags = listOf("Fantasy", "Fantasy", null, "Sci-Fi"),
                ),
            )

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.genres.slices.map { it.name to it.count }.toSet() shouldBe setOf("Fantasy" to 1, "Sci-Fi" to 1)
            result.genres.taggedBookCount shouldBe 1
            result.genres.slices.forEach { it.fraction shouldBe (1.0 plusOrMinus 0.0001) }
        }
    }

    @Nested
    inner class MeMapping {
        @Test
        fun `recentlyLoved joins non-null author names and formats the month label`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(
                me(
                    recentlyLoved = listOf(
                        lovedEntry(
                            rating = 4.5,
                            lastReadDate = "2026-05-04",
                            title = "Dune",
                            imageUrl = "cover1.jpg",
                            authorNames = listOf("Frank Herbert", null, "Someone Else"),
                        ),
                    ),
                ),
            )
            mockEmptyStatsQuery()

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.recentlyLoved shouldBe listOf(
                LovedBook(
                    bookId = 1,
                    coverUrl = "cover1.jpg",
                    title = "Dune",
                    author = "Frank Herbert, Someone Else",
                    ratingStars = 4.5,
                    monthLabel = "May 2026",
                ),
            )
        }

        @Test
        fun `recentlyLoved falls back to empty strings and a zero rating when fields are null`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(
                me(
                    recentlyLoved = listOf(
                        lovedEntry(
                            rating = null,
                            lastReadDate = null,
                            title = null,
                            imageUrl = null,
                            authorNames = emptyList(),
                        ),
                    ),
                ),
            )
            mockEmptyStatsQuery()

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.recentlyLoved shouldBe listOf(
                LovedBook(
                    bookId = 1,
                    coverUrl = "",
                    title = "",
                    author = "",
                    ratingStars = 0.0,
                    monthLabel = "",
                ),
            )
        }

        @Test
        fun `recentlyLoved prefers the picked edition cover over the book cover`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(
                me(
                    recentlyLoved = listOf(
                        lovedEntry(
                            rating = 4.0,
                            lastReadDate = null,
                            title = "Dune",
                            imageUrl = "book.jpg",
                            authorNames = emptyList(),
                            edition = lovedEdition(imageUrl = "edition.jpg"),
                        ),
                    ),
                ),
            )
            mockEmptyStatsQuery()

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.recentlyLoved.map { it.coverUrl } shouldBe listOf("edition.jpg")
        }

        @Test
        fun `recentlyLoved uses the book cover when there is no picked edition`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(
                me(
                    recentlyLoved = listOf(
                        lovedEntry(
                            rating = 4.0,
                            lastReadDate = null,
                            title = "Dune",
                            imageUrl = "book.jpg",
                            authorNames = emptyList(),
                            edition = null,
                        ),
                    ),
                ),
            )
            mockEmptyStatsQuery()

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.recentlyLoved.map { it.coverUrl } shouldBe listOf("book.jpg")
        }

        @Test
        fun `recentlyLoved uses the book cover when the picked edition has no image`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(
                me(
                    recentlyLoved = listOf(
                        lovedEntry(
                            rating = 4.0,
                            lastReadDate = null,
                            title = "Dune",
                            imageUrl = "book.jpg",
                            authorNames = emptyList(),
                            edition = lovedEdition(imageUrl = null),
                        ),
                    ),
                ),
            )
            mockEmptyStatsQuery()

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.recentlyLoved.map { it.coverUrl } shouldBe listOf("book.jpg")
        }

        @Test
        fun `recentlyLoved has an empty cover when neither edition nor book has an image`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(
                me(
                    recentlyLoved = listOf(
                        lovedEntry(
                            rating = 4.0,
                            lastReadDate = null,
                            title = "Dune",
                            imageUrl = null,
                            authorNames = emptyList(),
                            edition = lovedEdition(imageUrl = null),
                        ),
                    ),
                ),
            )
            mockEmptyStatsQuery()

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.recentlyLoved.map { it.coverUrl } shouldBe listOf("")
        }
    }

    @Nested
    inner class AuthorDemographicsMapping {
        @Test
        fun `gender_id 1, 2 and 3 map to Male, Female and Other, while null and an unrecognized id both map to Unknown`() =
            runTest {
                // ----- Arrange -----
                mockProfileQuery(me())
                coEvery {
                    apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
                } returns statsPage(
                    statsUserBook(
                        bookPages = 100,
                        contributions = listOf(
                            contribution(
                                id = 1,
                                genderId = 1,
                            ),
                            contribution(
                                id = 2,
                                genderId = 2,
                            ),
                            contribution(
                                id = 3,
                                genderId = 3,
                            ),
                            contribution(
                                id = 4,
                                genderId = null,
                            ),
                            contribution(
                                id = 5,
                                genderId = 99,
                            ),
                        ),
                    ),
                )

                // ----- Act -----
                val result = dataSource.getUserProfileSnapshot()

                // ----- Assert -----
                result.authorDemographics.knownGenderCount shouldBe 3
                result.authorDemographics.unknownGenderCount shouldBe 2
                result.authorDemographics.genderSlices.last() shouldBe GenderSlice(
                    gender = Gender.Unknown,
                    count = 2,
                    fraction = 2.0 / 5.0,
                )
                result.authorDemographics.genderSlices.dropLast(1).map { it.gender to it.count }.toSet() shouldBe setOf(
                    Gender.Male to 1,
                    Gender.Female to 1,
                    Gender.Other to 1,
                )
                result.authorDemographics.genderSlices.dropLast(1).forEach {
                    it.fraction shouldBe (1.0 / 5.0 plusOrMinus 0.0001)
                }
            }

        @Test
        fun `a contribution with a null author is skipped rather than crashing or counting as an author`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(me())
            coEvery {
                apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
            } returns statsPage(
                statsUserBook(
                    bookPages = 100,
                    contributions = listOf(
                        contribution(
                            id = 1,
                            genderId = 1,
                        ),
                        nullAuthorContribution(),
                    ),
                ),
            )

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.authorDemographics.knownGenderCount shouldBe 1
            result.authorDemographics.unknownGenderCount shouldBe 0
        }

        @Test
        fun `is_bipoc and is_lgbtq true, false and null across distinct authors land in the matching yes-no-unknown buckets`() =
            runTest {
                // ----- Arrange -----
                mockProfileQuery(me())
                coEvery {
                    apolloClient.safeQuery(query = ofType<GetReadUserBooksForStatsQuery>())
                } returns statsPage(
                    statsUserBook(
                        bookPages = 100,
                        contributions = listOf(
                            contribution(
                                id = 1,
                                isBipoc = true,
                                isLgbtq = null,
                                genderId = 1,
                            ),
                            contribution(
                                id = 2,
                                isBipoc = false,
                                isLgbtq = true,
                                genderId = 2,
                            ),
                            contribution(
                                id = 3,
                                isBipoc = null,
                                isLgbtq = false,
                                genderId = null,
                            ),
                        ),
                    ),
                )

                // ----- Act -----
                val result = dataSource.getUserProfileSnapshot()

                // ----- Assert -----
                result.authorDemographics.bipocBreakdown shouldBe DemographicBreakdown(
                    yesCount = 1,
                    noCount = 1,
                    unknownCount = 1,
                )
                result.authorDemographics.lgbtqBreakdown shouldBe DemographicBreakdown(
                    yesCount = 1,
                    noCount = 1,
                    unknownCount = 1,
                )
            }
    }

    @Nested
    inner class StreamReadingDaysDescending {
        private fun readingActivityPage(vararg actionAts: String): GetReadingActivityDaysQuery.Data =
            GetReadingActivityDaysQuery.Data(
                reading_journals = actionAts.map {
                    GetReadingActivityDaysQuery.Data.Reading_journal(
                        __typename = "reading_journals",
                        action_at = it,
                    )
                },
            )

        @Test
        fun `emits parsed dates across pages until an empty page ends the stream`() = runTest {
            // ----- Arrange -----
            val page1 = readingActivityPage(
                "2026-05-04",
                "2026-05-03",
            )
            val page2 = readingActivityPage(
                "2026-05-02",
                "2026-05-01",
            )
            val emptyPage = readingActivityPage()

            coEvery {
                apolloClient.safeQuery(query = any<GetReadingActivityDaysQuery>())
            } returnsMany listOf(page1, page2, emptyPage)

            // ----- Act -----
            val result = dataSource.streamReadingDaysDescending(userId = 42).toList()

            // ----- Assert -----
            result shouldBe listOf(
                LocalDate(
                    2026,
                    5,
                    4,
                ),
                LocalDate(
                    2026,
                    5,
                    3,
                ),
                LocalDate(
                    2026,
                    5,
                    2,
                ),
                LocalDate(
                    2026,
                    5,
                    1,
                ),
            )
            coVerify(exactly = 3) {
                apolloClient.safeQuery(query = any<GetReadingActivityDaysQuery>())
            }
        }

        @Test
        fun `advances offset by raw row count including unparseable rows`() = runTest {
            // ----- Arrange -----
            val page1 = readingActivityPage(
                "2026-05-04",
                "not-a-date",
                "2026-05-03",
            )
            val page2 = readingActivityPage()

            coEvery {
                apolloClient.safeQuery(query = any<GetReadingActivityDaysQuery>())
            } returnsMany listOf(page1, page2)

            // ----- Act -----
            val result = dataSource.streamReadingDaysDescending(userId = 42).toList()

            // ----- Assert -----
            result shouldBe listOf(
                LocalDate(
                    2026,
                    5,
                    4,
                ),
                LocalDate(
                    2026,
                    5,
                    3,
                ),
            )
            coVerify {
                apolloClient.safeQuery(
                    query = GetReadingActivityDaysQuery(
                        userId = 42,
                        limit = 100,
                        offset = 3,
                    ),
                )
            }
        }

        @Test
        fun `buckets timestamps by local timezone and passes bare dates through unchanged`() = runTest {
            // ----- Arrange -----
            val utcMinus5 = UtcOffset(hours = -5).asTimeZone()
            val localDataSource = ProfileRemoteDataSourceImpl(
                apolloClient = apolloClient,
                clock = fixedClock,
                timeZone = utcMinus5,
            )

            val page = readingActivityPage(
                "2026-05-17T23:15:05+00:00",
                "2026-05-19T04:00:00+00:00",
                "2026-05-02",
            )
            val emptyPage = readingActivityPage()

            coEvery {
                apolloClient.safeQuery(query = any<GetReadingActivityDaysQuery>())
            } returnsMany listOf(page, emptyPage)

            // ----- Act -----
            val result = localDataSource.streamReadingDaysDescending(userId = 42).toList()

            // ----- Assert -----
            result shouldBe listOf(
                LocalDate(
                    2026,
                    5,
                    17,
                ),
                LocalDate(
                    2026,
                    5,
                    18,
                ),
                LocalDate(
                    2026,
                    5,
                    2,
                ),
            )
        }
    }
}
