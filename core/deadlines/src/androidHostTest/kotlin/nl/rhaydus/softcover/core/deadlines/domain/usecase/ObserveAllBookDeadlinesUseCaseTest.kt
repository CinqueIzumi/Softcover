package nl.rhaydus.softcover.core.deadlines.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.deadlines.domain.repository.BookDeadlineRepository
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

class ObserveAllBookDeadlinesUseCaseTest {
    private lateinit var repository: BookDeadlineRepository
    private lateinit var useCase: ObserveAllBookDeadlinesUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = ObserveAllBookDeadlinesUseCase(repository = repository)
    }

    private fun buildDeadline(bookId: Int) = BookDeadline(
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
        fun `associates deadlines by bookId`() = runTest {
            // ----- Arrange -----
            val deadline1 = buildDeadline(bookId = 1)
            val deadline2 = buildDeadline(bookId = 2)

            every {
                repository.observeAll()
            } returns flowOf(listOf(deadline1, deadline2))

            // ----- Act & Assert -----
            useCase().test {
                val map = awaitItem()
                map[1] shouldBe deadline1
                map[2] shouldBe deadline2
                awaitComplete()
            }
        }

        @Test
        fun `emits empty map when repository emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                repository.observeAll()
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyMap()
                awaitComplete()
            }
        }

        @Test
        fun `map key matches the bookId of the deadline value`() = runTest {
            // ----- Arrange -----
            val deadline = buildDeadline(bookId = 99)

            every {
                repository.observeAll()
            } returns flowOf(listOf(deadline))

            // ----- Act & Assert -----
            useCase().test {
                val map = awaitItem()
                map.keys.first() shouldBe 99
                map.values.first().bookId shouldBe 99
                awaitComplete()
            }
        }
    }
}
