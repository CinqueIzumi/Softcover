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
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.datedRead
import nl.rhaydus.softcover.core.profile.domain.mapper.readBook
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveTimeToFinishUseCaseTest {
    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var dispatcher: TestDispatcher
    private lateinit var useCase: ObserveTimeToFinishUseCase

    @BeforeEach
    fun setUp() {
        getAllUserBooksUseCase = mockk()
        dispatcher = StandardTestDispatcher()
        useCase = ObserveTimeToFinishUseCase(
            getAllUserBooksUseCase = getAllUserBooksUseCase,
            appDispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            ),
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `a bounded range counts in-range reads and excludes only in-range failing reads`() = runTest(dispatcher) {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 10,
                            startedAt = "2025-12-20",
                            finishedAt = "2025-12-31",
                        ),
                        datedRead(
                            id = 11,
                            startedAt = "2026-01-01",
                            finishedAt = "2026-01-05",
                        ),
                        datedRead(
                            id = 12,
                            startedAt = null,
                            finishedAt = "2026-02-01",
                        ),
                        datedRead(
                            id = 13,
                            startedAt = null,
                            finishedAt = "2025-02-01",
                        ),
                    ),
                ),
            )
            every {
                getAllUserBooksUseCase()
            } returns flowOf(books)
            val range = StatsRange.Bounded(
                start = LocalDate.parse("2026-01-01"),
                endInclusive = LocalDate.parse("2026-12-31"),
            )

            // ----- Act & Assert -----
            useCase(range).test {
                val result = awaitItem()

                listOf(result.readCount, result.medianDays, result.excludedReadCount) shouldBe listOf(1, 5, 1)
                awaitComplete()
            }
        }

        @Test
        fun `an unbounded range counts every read and every excluded read`() = runTest(dispatcher) {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 10,
                            startedAt = "2019-01-01",
                            finishedAt = "2019-01-02",
                        ),
                        datedRead(
                            id = 11,
                            startedAt = "2026-01-01",
                            finishedAt = "2026-01-05",
                        ),
                        datedRead(
                            id = 12,
                            startedAt = null,
                            finishedAt = "2020-02-01",
                        ),
                    ),
                ),
            )
            every {
                getAllUserBooksUseCase()
            } returns flowOf(books)

            // ----- Act & Assert -----
            useCase(StatsRange.Unbounded).test {
                val result = awaitItem()

                listOf(result.readCount, result.medianDays, result.excludedReadCount) shouldBe listOf(2, 4, 1)
                awaitComplete()
            }
        }
    }
}
