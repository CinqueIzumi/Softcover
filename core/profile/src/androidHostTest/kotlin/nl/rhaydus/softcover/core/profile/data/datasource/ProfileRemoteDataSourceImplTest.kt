package nl.rhaydus.softcover.core.profile.data.datasource

import com.apollographql.apollo.ApolloClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.GetReadingActivityDaysQuery
import nl.rhaydus.softcover.GetUserProfileDataQuery
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

class ProfileRemoteDataSourceImplTest {
    private lateinit var apolloClient: ApolloClient
    private lateinit var dataSource: ProfileRemoteDataSourceImpl

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        dataSource = ProfileRemoteDataSourceImpl(
            apolloClient = apolloClient,
            timeZone = TimeZone.UTC,
        )

        mockkStatic("nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // Builds a User_books_page mock where the max progress is set.
    // status_id / edition / book values are irrelevant when max is present.
    private fun userBooksPageWithMax(maxProgress: Int): GetUserProfileDataQuery.Data.Me.User_books_page {
        val readsAggregate = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate>()
        val aggregate = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate.Aggregate>()
        val max = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate.Aggregate.Max>()
        val book = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.Book>()
        val page = mockk<GetUserProfileDataQuery.Data.Me.User_books_page>()

        every { page.user_book_reads_aggregate } returns readsAggregate
        every { readsAggregate.aggregate } returns aggregate
        every { aggregate.max } returns max
        every { max.progress_pages } returns maxProgress
        every { page.status_id } returns 2 // CURRENTLY_READING — not reached when max != null
        every { page.edition } returns null
        every { page.book } returns book
        every { book.pages } returns 0

        return page
    }

    // Builds a User_books_page mock where max progress is null.
    private fun userBooksPageNoMax(
        statusId: Int,
        editionPages: Int?,
        bookPages: Int?,
    ): GetUserProfileDataQuery.Data.Me.User_books_page {
        val readsAggregate = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate>()
        val aggregate = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate.Aggregate>()
        val max = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate.Aggregate.Max>()
        val book = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.Book>()
        val page = mockk<GetUserProfileDataQuery.Data.Me.User_books_page>()

        every { page.user_book_reads_aggregate } returns readsAggregate
        every { readsAggregate.aggregate } returns aggregate
        every { aggregate.max } returns max
        every { max.progress_pages } returns null
        every { page.status_id } returns statusId
        every { page.book } returns book
        every { book.pages } returns bookPages

        if (editionPages != null) {
            val edition = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.Edition>()
            every { page.edition } returns edition
            every { edition.pages } returns editionPages
        } else {
            every { page.edition } returns null
        }

        return page
    }

    @Nested
    inner class GetUserProfileSnapshot {
        @Test
        fun `maps all fields correctly when query returns full data`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val meEntry = mockk<GetUserProfileDataQuery.Data.Me>()
            val image = mockk<GetUserProfileDataQuery.Data.Me.Image>()
            val booksRead = mockk<GetUserProfileDataQuery.Data.Me.Books_read>()
            val booksReadAggregate = mockk<GetUserProfileDataQuery.Data.Me.Books_read.Aggregate>()
            val ratedBooks = mockk<GetUserProfileDataQuery.Data.Me.Rated_books>()
            val ratedBooksAggregate = mockk<GetUserProfileDataQuery.Data.Me.Rated_books.Aggregate>()
            val ratedBooksAvg = mockk<GetUserProfileDataQuery.Data.Me.Rated_books.Aggregate.Avg>()

            // One page with max progress 12500 so totalPagesRead sums to 12500.
            // status_id=2 (CURRENTLY_READING) — the edition/book fallback is never reached.
            val userBooksPage = userBooksPageWithMax(maxProgress = 12500)

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every { queryData.me } returns listOf(meEntry)
            every { meEntry.user_books_pages } returns listOf(userBooksPage)
            every { meEntry.name } returns "John Doe"
            every { meEntry.username } returns "johndoe"
            every { meEntry.bio } returns "A bio"
            every { meEntry.image } returns image
            every { image.url } returns "https://example.com/avatar.jpg"
            every { meEntry.books_read } returns booksRead
            every { booksRead.aggregate } returns booksReadAggregate
            every { booksReadAggregate.count } returns 42
            every { meEntry.rated_books } returns ratedBooks
            every { ratedBooks.aggregate } returns ratedBooksAggregate
            every { ratedBooksAggregate.avg } returns ratedBooksAvg
            every { ratedBooksAvg.rating } returns 3.75

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result shouldBe UserProfileSnapshot(
                profileImageUrl = "https://example.com/avatar.jpg",
                name = "John Doe",
                username = "johndoe",
                bio = "A bio",
                booksRead = 42,
                totalPagesRead = 12500,
                averageRating = 3.75,
            )
        }

        @Test
        fun `maps null name and bio to empty strings`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val meEntry = mockk<GetUserProfileDataQuery.Data.Me>()
            val booksRead = mockk<GetUserProfileDataQuery.Data.Me.Books_read>()
            val booksReadAggregate = mockk<GetUserProfileDataQuery.Data.Me.Books_read.Aggregate>()
            val ratedBooks = mockk<GetUserProfileDataQuery.Data.Me.Rated_books>()
            val ratedBooksAggregate = mockk<GetUserProfileDataQuery.Data.Me.Rated_books.Aggregate>()
            val ratedBooksAvg = mockk<GetUserProfileDataQuery.Data.Me.Rated_books.Aggregate.Avg>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every { queryData.me } returns listOf(meEntry)
            every { meEntry.user_books_pages } returns emptyList()
            every { meEntry.name } returns null
            every { meEntry.username } returns null
            every { meEntry.bio } returns null
            every { meEntry.image } returns null
            every { meEntry.books_read } returns booksRead
            every { booksRead.aggregate } returns booksReadAggregate
            every { booksReadAggregate.count } returns 0
            every { meEntry.rated_books } returns ratedBooks
            every { ratedBooks.aggregate } returns ratedBooksAggregate
            every { ratedBooksAggregate.avg } returns ratedBooksAvg
            every { ratedBooksAvg.rating } returns null

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.name shouldBe ""
            result.username shouldBe ""
            result.bio shouldBe ""
        }

        @Test
        fun `maps null image to empty profileImageUrl`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val meEntry = mockk<GetUserProfileDataQuery.Data.Me>()
            val booksRead = mockk<GetUserProfileDataQuery.Data.Me.Books_read>()
            val booksReadAggregate = mockk<GetUserProfileDataQuery.Data.Me.Books_read.Aggregate>()
            val ratedBooks = mockk<GetUserProfileDataQuery.Data.Me.Rated_books>()
            val ratedBooksAggregate = mockk<GetUserProfileDataQuery.Data.Me.Rated_books.Aggregate>()
            val ratedBooksAvg = mockk<GetUserProfileDataQuery.Data.Me.Rated_books.Aggregate.Avg>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every { queryData.me } returns listOf(meEntry)
            every { meEntry.user_books_pages } returns emptyList()
            every { meEntry.name } returns "Jane"
            every { meEntry.username } returns "jane"
            every { meEntry.bio } returns "Bio"
            every { meEntry.image } returns null
            every { meEntry.books_read } returns booksRead
            every { booksRead.aggregate } returns booksReadAggregate
            every { booksReadAggregate.count } returns 0
            every { meEntry.rated_books } returns ratedBooks
            every { ratedBooks.aggregate } returns ratedBooksAggregate
            every { ratedBooksAggregate.avg } returns ratedBooksAvg
            every { ratedBooksAvg.rating } returns null

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.profileImageUrl shouldBe ""
        }

        @Test
        fun `maps null image url to empty profileImageUrl`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val meEntry = mockk<GetUserProfileDataQuery.Data.Me>()
            val image = mockk<GetUserProfileDataQuery.Data.Me.Image>()
            val booksRead = mockk<GetUserProfileDataQuery.Data.Me.Books_read>()
            val booksReadAggregate = mockk<GetUserProfileDataQuery.Data.Me.Books_read.Aggregate>()
            val ratedBooks = mockk<GetUserProfileDataQuery.Data.Me.Rated_books>()
            val ratedBooksAggregate = mockk<GetUserProfileDataQuery.Data.Me.Rated_books.Aggregate>()
            val ratedBooksAvg = mockk<GetUserProfileDataQuery.Data.Me.Rated_books.Aggregate.Avg>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every { queryData.me } returns listOf(meEntry)
            every { meEntry.user_books_pages } returns emptyList()
            every { meEntry.name } returns "Jane"
            every { meEntry.username } returns "jane"
            every { meEntry.bio } returns "Bio"
            every { meEntry.image } returns image
            every { image.url } returns null
            every { meEntry.books_read } returns booksRead
            every { booksRead.aggregate } returns booksReadAggregate
            every { booksReadAggregate.count } returns 0
            every { meEntry.rated_books } returns ratedBooks
            every { ratedBooks.aggregate } returns ratedBooksAggregate
            every { ratedBooksAggregate.avg } returns ratedBooksAvg
            every { ratedBooksAvg.rating } returns null

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.profileImageUrl shouldBe ""
        }

        @Test
        fun `maps null aggregate counts and sums to zero fallbacks`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val meEntry = mockk<GetUserProfileDataQuery.Data.Me>()
            val booksRead = mockk<GetUserProfileDataQuery.Data.Me.Books_read>()
            val ratedBooks = mockk<GetUserProfileDataQuery.Data.Me.Rated_books>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every { queryData.me } returns listOf(meEntry)
            every { meEntry.user_books_pages } returns emptyList()
            every { meEntry.name } returns "Jane"
            every { meEntry.username } returns "jane"
            every { meEntry.bio } returns "Bio"
            every { meEntry.image } returns null
            every { meEntry.books_read } returns booksRead
            every { booksRead.aggregate } returns null
            every { meEntry.rated_books } returns ratedBooks
            every { ratedBooks.aggregate } returns null

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.booksRead shouldBe 0
            result.totalPagesRead shouldBe 0
            result.averageRating shouldBe 0.0
        }

        @Test
        fun `throws when me list is empty`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every { queryData.me } returns emptyList()

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.getUserProfileSnapshot()
            }
        }

        @Test
        fun `throws when safeQuery throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } throws expectedError

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                dataSource.getUserProfileSnapshot()
            }
        }
    }

    @Nested
    inner class TotalPagesRead {
        private fun arrangeQueryData(
            userBooksPages: List<GetUserProfileDataQuery.Data.Me.User_books_page>,
        ): GetUserProfileDataQuery.Data {
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val meEntry = mockk<GetUserProfileDataQuery.Data.Me>()
            val booksRead = mockk<GetUserProfileDataQuery.Data.Me.Books_read>()
            val ratedBooks = mockk<GetUserProfileDataQuery.Data.Me.Rated_books>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every { queryData.me } returns listOf(meEntry)
            every { meEntry.user_books_pages } returns userBooksPages
            every { meEntry.name } returns ""
            every { meEntry.username } returns ""
            every { meEntry.bio } returns ""
            every { meEntry.image } returns null
            every { meEntry.books_read } returns booksRead
            every { booksRead.aggregate } returns null
            every { meEntry.rated_books } returns ratedBooks
            every { ratedBooks.aggregate } returns null

            return queryData
        }

        @Test
        fun `empty list returns 0`() = runTest {
            // ----- Arrange -----
            arrangeQueryData(userBooksPages = emptyList())

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe 0
        }

        @Test
        fun `single page with max progress contributes max regardless of status`() = runTest {
            // ----- Arrange -----
            // status_id=2 (CURRENTLY_READING) — the edition/book fallback is never reached when max != null
            val page = userBooksPageWithMax(maxProgress = 250)
            arrangeQueryData(userBooksPages = listOf(page))

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe 250
        }

        @Test
        fun `single READ page with no max uses edition pages`() = runTest {
            // ----- Arrange -----
            // status_id=3 (READ), max=null → falls back to edition.pages=300
            val page = userBooksPageNoMax(
                statusId = 3,
                editionPages = 300,
                bookPages = 999,
            )
            arrangeQueryData(userBooksPages = listOf(page))

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe 300
        }

        @Test
        fun `single READ page with no max and no edition uses book pages`() = runTest {
            // ----- Arrange -----
            // status_id=3 (READ), max=null, edition=null → falls back to book.pages=280
            val page = userBooksPageNoMax(
                statusId = 3,
                editionPages = null,
                bookPages = 280,
            )
            arrangeQueryData(userBooksPages = listOf(page))

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe 280
        }

        @Test
        fun `single READ page with no max and no pages anywhere contributes 0`() = runTest {
            // ----- Arrange -----
            // status_id=3 (READ), max=null, edition.pages=null, book.pages=null → 0
            val page = userBooksPageNoMax(
                statusId = 3,
                editionPages = null,
                bookPages = null,
            )
            arrangeQueryData(userBooksPages = listOf(page))

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe 0
        }

        @Test
        fun `single page with non-READ status and no max contributes 0`() = runTest {
            // ----- Arrange -----
            // status_id=1 (WANT_TO_READ), max=null → contributes 0, edition/book pages ignored
            val page = userBooksPageNoMax(
                statusId = 1,
                editionPages = null,
                bookPages = 400,
            )
            arrangeQueryData(userBooksPages = listOf(page))

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe 0
        }

        @Test
        fun `READ status with partial max progress credits full edition pages`() = runTest {
            // ----- Arrange -----
            // status_id=3 (READ), max=50, edition.pages=300, book.pages=999
            // max(50, max(300, 999 fallback)) → edition wins over partial progress → 300
            val readsAggregate = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate>()
            val aggregate = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate.Aggregate>()
            val max = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate.Aggregate.Max>()
            val edition = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.Edition>()
            val book = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.Book>()
            val page = mockk<GetUserProfileDataQuery.Data.Me.User_books_page>()

            every { page.user_book_reads_aggregate } returns readsAggregate
            every { readsAggregate.aggregate } returns aggregate
            every { aggregate.max } returns max
            every { max.progress_pages } returns 50
            every { page.status_id } returns 3
            every { page.edition } returns edition
            every { edition.pages } returns 300
            every { page.book } returns book
            every { book.pages } returns 999

            arrangeQueryData(userBooksPages = listOf(page))

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe 300
        }

        @Test
        fun `READ status with max progress higher than edition pages credits the max progress`() = runTest {
            // ----- Arrange -----
            // status_id=3 (READ), max=350, edition.pages=300, book.pages=100
            // max(350, 300) → max progress wins → 350
            val readsAggregate = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate>()
            val aggregate = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate.Aggregate>()
            val max = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.User_book_reads_aggregate.Aggregate.Max>()
            val edition = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.Edition>()
            val book = mockk<GetUserProfileDataQuery.Data.Me.User_books_page.Book>()
            val page = mockk<GetUserProfileDataQuery.Data.Me.User_books_page>()

            every { page.user_book_reads_aggregate } returns readsAggregate
            every { readsAggregate.aggregate } returns aggregate
            every { aggregate.max } returns max
            every { max.progress_pages } returns 350
            every { page.status_id } returns 3
            every { page.edition } returns edition
            every { edition.pages } returns 300
            every { page.book } returns book
            every { book.pages } returns 100

            arrangeQueryData(userBooksPages = listOf(page))

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe 350
        }

        @Test
        fun `mixed list sums correctly across all three rule branches`() = runTest {
            // ----- Arrange -----
            // Page 1: max=200, status=2 (CURRENTLY_READING) → contributes 200
            // Page 2: max=null, status=3 (READ), edition.pages=300 → contributes 300
            // Page 3: max=null, status=1 (WANT_TO_READ), book.pages=999, edition=null → contributes 0
            // Total: 500
            val page1 = userBooksPageWithMax(maxProgress = 200)
            val page2 = userBooksPageNoMax(
                statusId = 3,
                editionPages = 300,
                bookPages = 999,
            )
            val page3 = userBooksPageNoMax(
                statusId = 1,
                editionPages = null,
                bookPages = 999,
            )
            arrangeQueryData(userBooksPages = listOf(page1, page2, page3))

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.totalPagesRead shouldBe 500
        }
    }

    @Nested
    inner class StreamReadingDaysDescending {
        private fun readingJournal(actionAt: String): GetReadingActivityDaysQuery.Data.Reading_journal {
            val row = mockk<GetReadingActivityDaysQuery.Data.Reading_journal>()

            every { row.action_at } returns actionAt

            return row
        }

        private fun readingActivityPage(vararg actionAts: String): GetReadingActivityDaysQuery.Data {
            val page = mockk<GetReadingActivityDaysQuery.Data>()

            every { page.reading_journals } returns actionAts.map { readingJournal(it) }

            return page
        }

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
            // 3 raw rows (one unparseable) → offset must advance by 3, not by the 2 parsed dates.
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
                timeZone = utcMinus5,
            )

            // 2026-05-17T23:15:05Z → UTC-5: 18:15 on 2026-05-17
            // 2026-05-19T04:00:00Z → UTC-5: 23:00 on 2026-05-18
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

        @Test
        fun `empty first page emits nothing and makes a single call`() = runTest {
            // ----- Arrange -----
            val emptyPage = readingActivityPage()

            coEvery {
                apolloClient.safeQuery(query = any<GetReadingActivityDaysQuery>())
            } returns emptyPage

            // ----- Act -----
            val result = dataSource.streamReadingDaysDescending(userId = 42).toList()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 1) {
                apolloClient.safeQuery(query = any<GetReadingActivityDaysQuery>())
            }
        }
    }
}
