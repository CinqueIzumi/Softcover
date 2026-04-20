package nl.rhaydus.softcover.feature.deadlines.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.deadlines.domain.repository.BookDeadlineRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SetBookDeadlineUseCaseTest {

    private lateinit var repository: BookDeadlineRepository
    private lateinit var useCase: SetBookDeadlineUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = SetBookDeadlineUseCase(repository = repository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository with the provided arguments`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val deadlineDate = LocalDate.of(2026, 7, 1)
            val currentPage = 50
            val totalPages = 300

            // ----- Act -----
            useCase(
                bookId = bookId,
                deadlineDate = deadlineDate,
                currentPage = currentPage,
                totalPages = totalPages,
            )

            // ----- Assert -----
            coVerify {
                repository.setDeadline(
                    bookId = bookId,
                    deadlineDate = deadlineDate,
                    currentPage = currentPage,
                    totalPages = totalPages,
                )
            }
        }

        @Test
        fun `returns success when repository setDeadline completes normally`() = runTest {
            // ----- Arrange -----
            val bookId = 1
            val deadlineDate = LocalDate.of(2026, 6, 1)

            coEvery {
                repository.setDeadline(
                    bookId = bookId,
                    deadlineDate = deadlineDate,
                    currentPage = any(),
                    totalPages = any(),
                )
            } returns Unit

            // ----- Act -----
            val result = useCase(
                bookId = bookId,
                deadlineDate = deadlineDate,
                currentPage = 0,
                totalPages = 200,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when repository setDeadline throws`() = runTest {
            // ----- Arrange -----
            val bookId = 2
            val deadlineDate = LocalDate.of(2026, 6, 1)
            val expectedError = RuntimeException("db error")

            coEvery {
                repository.setDeadline(
                    bookId = bookId,
                    deadlineDate = deadlineDate,
                    currentPage = any(),
                    totalPages = any(),
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                bookId = bookId,
                deadlineDate = deadlineDate,
                currentPage = 0,
                totalPages = 200,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
