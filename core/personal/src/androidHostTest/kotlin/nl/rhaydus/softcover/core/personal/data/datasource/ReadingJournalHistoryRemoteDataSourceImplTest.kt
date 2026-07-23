package nl.rhaydus.softcover.core.personal.data.datasource

import com.apollographql.apollo.ApolloClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.GetReadingJournalHistoryQuery
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry

class ReadingJournalHistoryRemoteDataSourceImplTest {
    private lateinit var apolloClient: ApolloClient
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var dataSource: ReadingJournalHistoryRemoteDataSourceImpl

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        getUserIdUseCase = mockk()
        dataSource =
            ReadingJournalHistoryRemoteDataSourceImpl(
                apolloClient = apolloClient,
                getUserIdUseCase = getUserIdUseCase,
            )

        mockkStatic("nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    /**
     * Builds a row that the real [nl.rhaydus.softcover.core.personal.data.mapper.toReadingJournalEntry]
     * maps into a distinct [ReadingJournalEntry], keyed by [pages], so accumulated pagination results
     * can be asserted by value and order without mocking the mapper itself.
     */
    private fun mockRow(pages: Int): GetReadingJournalHistoryQuery.Data.Reading_journal {
        val row = mockk<GetReadingJournalHistoryQuery.Data.Reading_journal>()

        every {
            row.updated_at
        } returns "2024-01-01"
        every {
            row.metadata
        } returns mapOf("progress_pages" to pages)

        return row
    }

    private fun expectedEntry(pages: Int): ReadingJournalEntry = ReadingJournalEntry(
        date = LocalDate(
            2024,
            1,
            1,
        ),
        pages = pages,
        seconds = null,
    )

    @Nested
    inner class GetReadingJournalHistory {
        @Test
        fun `fetches a single page when fewer than PAGE_SIZE rows are returned`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val userId = 7
            val rows = (0 until 3).map { mockRow(pages = it) }
            val queryData = mockk<GetReadingJournalHistoryQuery.Data>()

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                apolloClient.safeQuery(query = any<GetReadingJournalHistoryQuery>())
            } returns queryData

            every {
                queryData.reading_journals
            } returns rows

            // ----- Act -----
            val result = dataSource.getReadingJournalHistory(bookId = bookId)

            // ----- Assert -----
            result shouldBe (0 until 3).map { expectedEntry(pages = it) }

            coVerify(exactly = 1) {
                apolloClient.safeQuery(query = any<GetReadingJournalHistoryQuery>())
            }
        }

        @Test
        fun `pages through a full first page and a short second page, using the accumulated offset`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val userId = 7
            val firstPageRows = (0 until 100).map { mockRow(pages = it) }
            val secondPageRows = (100 until 125).map { mockRow(pages = it) }
            val firstPageData = mockk<GetReadingJournalHistoryQuery.Data>()
            val secondPageData = mockk<GetReadingJournalHistoryQuery.Data>()

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
            } returns firstPageData

            coEvery {
                apolloClient.safeQuery(
                    query = GetReadingJournalHistoryQuery(
                        bookId = bookId,
                        userId = userId,
                        limit = 100,
                        offset = 100,
                    ),
                )
            } returns secondPageData

            every {
                firstPageData.reading_journals
            } returns firstPageRows

            every {
                secondPageData.reading_journals
            } returns secondPageRows

            // ----- Act -----
            val result = dataSource.getReadingJournalHistory(bookId = bookId)

            // ----- Assert -----
            result shouldBe (0 until 125).map { expectedEntry(pages = it) }

            coVerify(exactly = 2) {
                apolloClient.safeQuery(query = any<GetReadingJournalHistoryQuery>())
            }

            coVerify {
                apolloClient.safeQuery(
                    query = GetReadingJournalHistoryQuery(
                        bookId = bookId,
                        userId = userId,
                        limit = 100,
                        offset = 100,
                    ),
                )
            }
        }

        @Test
        fun `stops after a single call and returns an empty list when the first page is empty`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val userId = 7
            val queryData = mockk<GetReadingJournalHistoryQuery.Data>()

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                apolloClient.safeQuery(query = any<GetReadingJournalHistoryQuery>())
            } returns queryData

            every {
                queryData.reading_journals
            } returns emptyList()

            // ----- Act -----
            val result = dataSource.getReadingJournalHistory(bookId = bookId)

            // ----- Assert -----
            result shouldBe emptyList()

            coVerify(exactly = 1) {
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

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                dataSource.getReadingJournalHistory(bookId = bookId)
            }

            coVerify(exactly = 0) {
                apolloClient.safeQuery(query = any<GetReadingJournalHistoryQuery>())
            }
        }
    }
}
