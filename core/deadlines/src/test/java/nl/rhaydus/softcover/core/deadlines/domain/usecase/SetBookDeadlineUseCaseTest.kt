package nl.rhaydus.softcover.core.deadlines.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.deadlines.domain.repository.BookDeadlineRepository
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
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
            val deadlineDate = LocalDate.of(
                2026,
                7,
                1,
            )
            val current = 50
            val total = 300

            // ----- Act -----
            useCase(
                bookId = bookId,
                deadlineDate = deadlineDate,
                current = current,
                total = total,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            coVerify {
                repository.setDeadline(
                    bookId = bookId,
                    deadlineDate = deadlineDate,
                    current = current,
                    total = total,
                    unit = DeadlineUnit.PAGES,
                )
            }
        }

        @Test
        fun `returns success when repository setDeadline completes normally`() = runTest {
            // ----- Arrange -----
            val bookId = 1
            val deadlineDate = LocalDate.of(
                2026,
                6,
                1,
            )

            coEvery {
                repository.setDeadline(
                    bookId = bookId,
                    deadlineDate = deadlineDate,
                    current = any(),
                    total = any(),
                    unit = any(),
                )
            } returns Unit

            // ----- Act -----
            val result = useCase(
                bookId = bookId,
                deadlineDate = deadlineDate,
                current = 0,
                total = 200,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when repository setDeadline throws`() = runTest {
            // ----- Arrange -----
            val bookId = 2
            val deadlineDate = LocalDate.of(
                2026,
                6,
                1,
            )
            val expectedError = RuntimeException("db error")

            coEvery {
                repository.setDeadline(
                    bookId = bookId,
                    deadlineDate = deadlineDate,
                    current = any(),
                    total = any(),
                    unit = any(),
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                bookId = bookId,
                deadlineDate = deadlineDate,
                current = 0,
                total = 200,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `passes SECONDS unit to repository for audiobook deadline`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val deadlineDate = LocalDate.of(
                2026,
                8,
                1,
            )

            coEvery {
                repository.setDeadline(
                    bookId = bookId,
                    deadlineDate = deadlineDate,
                    current = any(),
                    total = any(),
                    unit = DeadlineUnit.SECONDS,
                )
            } returns Unit

            // ----- Act -----
            useCase(
                bookId = bookId,
                deadlineDate = deadlineDate,
                current = 0,
                total = 18000,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Assert -----
            coVerify {
                repository.setDeadline(
                    bookId = bookId,
                    deadlineDate = deadlineDate,
                    current = any(),
                    total = any(),
                    unit = DeadlineUnit.SECONDS,
                )
            }
        }
    }
}
