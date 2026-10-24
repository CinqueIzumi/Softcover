package nl.rhaydus.softcover.core.profile.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.readBook
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveReadingStatsUseCaseTest {
    private lateinit var getReadUserBooksUseCase: GetReadUserBooksUseCase
    private lateinit var useCase: ObserveReadingStatsUseCase

    @BeforeEach
    fun setUp() {
        getReadUserBooksUseCase = mockk()
        val dispatcher = UnconfinedTestDispatcher()
        useCase = ObserveReadingStatsUseCase(
            getReadUserBooksUseCase = getReadUserBooksUseCase,
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
        fun `aggregates each emission of the read books and skips repeats`() = runTest {
            // ----- Arrange -----
            val one = listOf(readBook(
                id = 1,
                finishedAt = "2026-01-01",
                pages = 100,
            ),)
            val two = one + readBook(
                id = 2,
                finishedAt = "2026-02-01",
                pages = 50,
            )
            every {
                getReadUserBooksUseCase()
            } returns flowOf(
                one,
                one,
                two,
            )

            // ----- Act & Assert -----
            useCase(StatsRange.Unbounded).test {
                awaitItem().totalPages shouldBe 100
                awaitItem().totalPages shouldBe 150
                awaitComplete()
            }
        }
    }
}
