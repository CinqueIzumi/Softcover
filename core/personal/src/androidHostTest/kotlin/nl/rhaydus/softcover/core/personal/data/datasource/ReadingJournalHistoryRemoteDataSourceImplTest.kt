package nl.rhaydus.softcover.core.personal.data.datasource

import com.apollographql.apollo.ApolloClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
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
