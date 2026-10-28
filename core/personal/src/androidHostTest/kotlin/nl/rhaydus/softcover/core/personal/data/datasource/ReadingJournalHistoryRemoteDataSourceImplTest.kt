package nl.rhaydus.softcover.core.personal.data.datasource

import com.apollographql.apollo.ApolloClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.GetAllReadingJournalHistoryQuery
import nl.rhaydus.softcover.GetReadingJournalHistoryQuery
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.network.helper.safeQuery
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ReadingJournalHistoryRemoteDataSourceImplTest {
    private lateinit var apolloClient: ApolloClient
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var dataSource: ReadingJournalHistoryRemoteDataSourceImpl

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
        getUserIdUseCase = mockk()
        dataSource =
            ReadingJournalHistoryRemoteDataSourceImpl(
                apolloClient = apolloClient,
                getUserIdUseCase = getUserIdUseCase,
            )
    }

    /**
     * Builds a row that the real [nl.rhaydus.softcover.core.personal.data.mapper.toProgressHistoryEntity]
     * maps into a distinct [ProgressHistoryEntity], keyed by [pages], so accumulated pagination results
     * can be asserted by value and order without mocking the mapper itself.
     */
    private fun row(pages: Int): GetReadingJournalHistoryQuery.Data.Reading_journal =
        GetReadingJournalHistoryQuery.Data.Reading_journal(
            __typename = "reading_journals",
            action_at = "2024-01-01T10:00:00+00:00",
            metadata = mapOf("progress_pages" to pages),
        )

    private fun expectedEntity(
        bookId: Int,
        pages: Int,
    ): ProgressHistoryEntity = ProgressHistoryEntity(
        bookId = bookId,
        actionAtEpochMillis = Instant.parse("2024-01-01T10:00:00Z").toEpochMilliseconds(),
        pages = pages,
        seconds = null,
        pending = false,
        pagesRead = pages,
    )

    private fun allRow(pages: Int): GetAllReadingJournalHistoryQuery.Data.Reading_journal =
        GetAllReadingJournalHistoryQuery.Data.Reading_journal(
            __typename = "reading_journals",
            book_id = 42,
            action_at = "2024-01-01T10:00:00+00:00",
            metadata = mapOf("progress_pages" to pages),
        )

    @Nested
    inner class GetAllReadingJournalHistory {
        @Test
        fun `never requests more than 100 rows per request`() = runTest {
            // ----- Arrange -----
            val requestedLimits = mutableListOf<Int>()
            val querySlot = slot<GetAllReadingJournalHistoryQuery>()

            coEvery {
                getUserIdUseCase()
            } returns Result.success(7)

            coEvery {
                apolloClient.safeQuery(query = capture(querySlot))
            } answers {
                requestedLimits += querySlot.captured.limit
                GetAllReadingJournalHistoryQuery.Data(reading_journals = emptyList())
            }

            // ----- Act -----
            dataSource.getAllReadingJournalHistory()

            // ----- Assert -----
            requestedLimits.isNotEmpty() shouldBe true
            requestedLimits.all { it <= 100 } shouldBe true
        }

        @Test
        fun `returns every row when the server caps each page at 100 rows`() = runTest {
            // ----- Arrange -----
            val serverRows = (0 until 250).map { allRow(pages = it) }
            val querySlot = slot<GetAllReadingJournalHistoryQuery>()

            coEvery {
                getUserIdUseCase()
            } returns Result.success(7)

            coEvery {
                apolloClient.safeQuery(query = capture(querySlot))
            } answers {
                val query = querySlot.captured
                GetAllReadingJournalHistoryQuery.Data(
                    reading_journals = serverRows
                        .drop(query.offset)
                        .take(
                            minOf(
                                query.limit,
                                100,
                            ),
                        ),
                )
            }

            // ----- Act -----
            val result = dataSource.getAllReadingJournalHistory()

            // ----- Assert -----
            result shouldHaveSize 250
            result shouldBe (0 until 250).map {
                expectedEntity(
                    bookId = 42,
                    pages = it,
                )
            }
        }
    }

    @Nested
    inner class GetReadingJournalHistory {
        @Test
        fun `fetches a single page when fewer than PAGE_SIZE rows are returned`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val userId = 7

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                apolloClient.safeQuery(query = any<GetReadingJournalHistoryQuery>())
            } returns GetReadingJournalHistoryQuery.Data(reading_journals = (0 until 3).map { row(pages = it) })

            // ----- Act -----
            val result = dataSource.getReadingJournalHistory(bookId = bookId)

            // ----- Assert -----
            result shouldBe (0 until 3).map {
                expectedEntity(
                    bookId = bookId,
                    pages = it,
                )
            }

            coVerify(exactly = 1) {
                apolloClient.safeQuery(query = any<GetReadingJournalHistoryQuery>())
            }
        }

        @Test
        fun `pages through a full first page and a short second page, using the accumulated offset`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val userId = 7

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                apolloClient.safeQuery(
                    query = GetReadingJournalHistoryQuery(
                        bookId = bookId,
                        userId = userId,
                        limit = 100,
                        offset = 0,
                    ),
                )
            } returns GetReadingJournalHistoryQuery.Data(reading_journals = (0 until 100).map { row(pages = it) })

            coEvery {
                apolloClient.safeQuery(
                    query = GetReadingJournalHistoryQuery(
                        bookId = bookId,
                        userId = userId,
                        limit = 100,
                        offset = 100,
                    ),
                )
            } returns GetReadingJournalHistoryQuery.Data(reading_journals = (100 until 125).map { row(pages = it) })

            // ----- Act -----
            val result = dataSource.getReadingJournalHistory(bookId = bookId)

            // ----- Assert -----
            result shouldBe (0 until 125).map {
                expectedEntity(
                    bookId = bookId,
                    pages = it,
                )
            }

            coVerify(exactly = 2) {
                apolloClient.safeQuery(query = any<GetReadingJournalHistoryQuery>())
            }
        }

        @Test
        fun `throws and never queries when GetUserIdUseCase returns failure`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val idError = RuntimeException("no user id")

            coEvery {
                getUserIdUseCase()
            } returns Result.failure(idError)

            // ----- Act -----
            val thrown = shouldThrow<RuntimeException> {
                dataSource.getReadingJournalHistory(bookId = bookId)
            }

            // ----- Assert -----
            thrown shouldBe idError

            coVerify(exactly = 0) {
                apolloClient.safeQuery(query = any<GetReadingJournalHistoryQuery>())
            }
        }
    }
}
