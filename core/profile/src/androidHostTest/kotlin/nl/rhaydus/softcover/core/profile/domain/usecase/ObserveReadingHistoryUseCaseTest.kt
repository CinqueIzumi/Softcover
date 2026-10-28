package nl.rhaydus.softcover.core.profile.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.profile.domain.mapper.readBook
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class ObserveReadingHistoryUseCaseTest {
    private val fixedClock: Clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-10-23T12:00:00Z")
    }

    private lateinit var getReadUserBooksUseCase: GetReadUserBooksUseCase
    private lateinit var useCase: ObserveReadingHistoryUseCase

    @BeforeEach
    fun setUp() {
        getReadUserBooksUseCase = mockk()
        val dispatcher = UnconfinedTestDispatcher()
        useCase = ObserveReadingHistoryUseCase(
            getReadUserBooksUseCase = getReadUserBooksUseCase,
            appDispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            ),
            clock = fixedClock,
            timeZone = TimeZone.UTC,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `builds the history against the clocks current year`() = runTest {
            // ----- Arrange -----
            val books = listOf(readBook(
                id = 1,
                finishedAt = "2025-05-01",
                pages = 10,
            ),)
            every {
                getReadUserBooksUseCase()
            } returns flowOf(books)

            // ----- Act & Assert -----
            useCase().test {
                val history = awaitItem()
                history.booksByYear.map { it.year } shouldBe listOf(2025, 2026)
                history.pagesByMonth.all { it.year == 2026 } shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `skips an emission that yields an equal history`() = runTest {
            // ----- Arrange -----
            val books = listOf(readBook(
                id = 1,
                finishedAt = "2025-05-01",
            ),)
            every {
                getReadUserBooksUseCase()
            } returns flowOf(
                books,
                books,
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().yearsRead shouldBe listOf(2025)
                awaitComplete()
            }
        }
    }
}
