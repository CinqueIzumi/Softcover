package nl.rhaydus.softcover.core.deadlines.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.deadlines.domain.repository.BookDeadlineRepository
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveBookDeadlineUseCaseTest {
    private lateinit var repository: BookDeadlineRepository
    private lateinit var useCase: ObserveBookDeadlineUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = ObserveBookDeadlineUseCase(repository = repository)
    }

    private fun buildDeadline(bookId: Int = 1) = BookDeadline(
        bookId = bookId,
        deadlineDate = LocalDate(
            2026,
            5,
            1,
        ),
        setAt = LocalDate(
            2026,
            4,
            1,
        ),
        initialPerDay = 10f,
        unit = DeadlineUnit.PAGES,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `emits the deadline returned by the repository`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            val expectedDeadline = buildDeadline(bookId = bookId)

            every {
                repository.observe(bookId = bookId)
            } returns flowOf(expectedDeadline)

            // ----- Act & Assert -----
            useCase(bookId = bookId).test {
                awaitItem() shouldBe expectedDeadline
                awaitComplete()
            }
        }
    }
}
