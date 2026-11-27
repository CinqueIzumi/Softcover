package nl.rhaydus.softcover.core.profile.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetPausedUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveProgressUpdatesUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookCopiesUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.domain.model.PricePaid
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.dnfBook
import nl.rhaydus.softcover.core.profile.domain.mapper.progressUpdate
import nl.rhaydus.softcover.core.profile.domain.mapper.readBook
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveSpendStatsUseCaseTest {
    private lateinit var getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase
    private lateinit var getPausedUserBooksUseCase: GetPausedUserBooksUseCase
    private lateinit var getReadUserBooksUseCase: GetReadUserBooksUseCase
    private lateinit var getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase
    private lateinit var observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase
    private lateinit var observeAllBookCopiesUseCase: ObserveAllBookCopiesUseCase
    private lateinit var dispatcher: TestDispatcher
    private lateinit var useCase: ObserveSpendStatsUseCase

    @BeforeEach
    fun setUp() {
        getCurrentlyReadingUserBooksUseCase = mockk()
        getPausedUserBooksUseCase = mockk()
        getReadUserBooksUseCase = mockk()
        getDidNotFinishUserBooksUseCase = mockk()
        observeProgressUpdatesUseCase = mockk()
        observeAllBookCopiesUseCase = mockk()

        every {
            getCurrentlyReadingUserBooksUseCase()
        } returns flowOf(emptyList<Book>())

        every {
            getPausedUserBooksUseCase()
        } returns flowOf(emptyList<Book>())

        every {
            getReadUserBooksUseCase()
        } returns flowOf(emptyList<Book>())

        every {
            getDidNotFinishUserBooksUseCase()
        } returns flowOf(emptyList<Book>())

        every {
            observeProgressUpdatesUseCase()
        } returns flowOf(emptyList<ProgressUpdate>())

        every {
            observeAllBookCopiesUseCase()
        } returns flowOf(emptyMap())

        dispatcher = StandardTestDispatcher()

        useCase = ObserveSpendStatsUseCase(
            getCurrentlyReadingUserBooksUseCase = getCurrentlyReadingUserBooksUseCase,
            getPausedUserBooksUseCase = getPausedUserBooksUseCase,
            getReadUserBooksUseCase = getReadUserBooksUseCase,
            getDidNotFinishUserBooksUseCase = getDidNotFinishUserBooksUseCase,
            observeProgressUpdatesUseCase = observeProgressUpdatesUseCase,
            observeAllBookCopiesUseCase = observeAllBookCopiesUseCase,
            appDispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            ),
            timeZone = TimeZone.UTC,
        )
    }

    private fun priced(
        id: Int,
        thousandths: Long,
    ) = id to BookCopy(
        bookId = id,
        price = PricePaid(
            amountThousandths = thousandths,
            currencyCode = "EUR",
        ),
    )

    @Nested
    inner class Invoke {
        @Test
        fun `all time spans the reading, paused, read and did not finish books`() = runTest(dispatcher) {
            // ----- Arrange -----
            every {
                getCurrentlyReadingUserBooksUseCase()
            } returns flowOf(listOf(readBook(id = 1)))

            every {
                getPausedUserBooksUseCase()
            } returns flowOf(listOf(readBook(id = 2)))

            every {
                getReadUserBooksUseCase()
            } returns flowOf(listOf(readBook(id = 3)))

            every {
                getDidNotFinishUserBooksUseCase()
            } returns flowOf(listOf(dnfBook(id = 4)))

            every {
                observeAllBookCopiesUseCase()
            } returns flowOf(
                mapOf(
                    priced(
                        1,
                        1_000,
                    ),
                    priced(
                        2,
                        2_000,
                    ),
                    priced(
                        3,
                        3_000,
                    ),
                    priced(
                        4,
                        4_000,
                    ),
                ),
            )

            // ----- Act & Assert -----
            useCase(StatsRange.Unbounded).test {
                val stats = awaitItem()

                listOf(stats.countedBooks, stats.pricedBooks, stats.currencies.single().totalThousandths) shouldBe
                    listOf(4, 4, 10_000L)

                awaitComplete()
            }
        }

        @Test
        fun `a window leaves out the books read outside it`() = runTest(dispatcher) {
            // ----- Arrange -----
            every {
                getReadUserBooksUseCase()
            } returns flowOf(
                listOf(
                    readBook(
                        id = 1,
                        finishedAt = "2026-03-10",
                    ),
                    readBook(
                        id = 2,
                        finishedAt = "2025-03-10",
                    ),
                ),
            )

            every {
                observeAllBookCopiesUseCase()
            } returns flowOf(
                mapOf(
                    priced(
                        1,
                        5_000,
                    ),
                    priced(
                        2,
                        7_000,
                    ),
                ),
            )

            val window = StatsRange.Bounded(
                LocalDate(
                    2026,
                    1,
                    1,
                ),
                LocalDate(
                    2026,
                    12,
                    31,
                ),
            )

            // ----- Act & Assert -----
            useCase(window).test {
                val stats = awaitItem()

                listOf(stats.countedBooks, stats.currencies.single().totalThousandths) shouldBe listOf(1, 5_000L)
                awaitComplete()
            }
        }

        @Test
        fun `a book started in the window counts through its progress update`() = runTest(dispatcher) {
            // ----- Arrange -----
            every {
                getCurrentlyReadingUserBooksUseCase()
            } returns flowOf(listOf(readBook(id = 1)))

            every {
                observeProgressUpdatesUseCase()
            } returns flowOf(
                listOf(
                    progressUpdate(
                        bookId = 1,
                        at = "2026-02-02T10:00:00Z",
                        pagesRead = 5,
                    ),
                ),
            )

            val window = StatsRange.Bounded(
                LocalDate(
                    2026,
                    2,
                    1,
                ),
                LocalDate(
                    2026,
                    2,
                    28,
                ),
            )

            // ----- Act & Assert -----
            useCase(window).test {
                awaitItem().countedBooks shouldBe 1
                awaitComplete()
            }
        }
    }
}
