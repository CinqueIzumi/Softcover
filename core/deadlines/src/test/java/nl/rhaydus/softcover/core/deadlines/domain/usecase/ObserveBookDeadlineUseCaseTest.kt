package nl.rhaydus.softcover.core.deadlines.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.deadlines.domain.repository.BookDeadlineRepository
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

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
        deadlineDate = LocalDate.of(
            2026,
            5,
            1,
        ),
        setAt = LocalDate.of(
            2026,
            4,
            1,
        ),
        initialPerDay = 10f,
        unit = nl.rhaydus.softcover.core.domain.model.DeadlineUnit.PAGES,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository observe with the given bookId`() = runTest {
            // ----- Arrange -----
            val bookId = 42

            every {
                repository.observe(bookId = bookId)
            } returns flowOf(null)

            // ----- Act -----
            useCase(bookId = bookId).test {
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            verify {
                repository.observe(bookId = bookId)
            }
        }

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

        @Test
        fun `emits null when repository emits null`() = runTest {
            // ----- Arrange -----
            val bookId = 10

            every {
                repository.observe(bookId = bookId)
            } returns flowOf(null)

            // ----- Act & Assert -----
            useCase(bookId = bookId).test {
                awaitItem() shouldBe null
                awaitComplete()
            }
        }
    }
}
