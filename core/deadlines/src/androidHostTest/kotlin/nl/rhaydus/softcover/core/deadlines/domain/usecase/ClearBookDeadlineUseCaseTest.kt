package nl.rhaydus.softcover.core.deadlines.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.deadlines.domain.repository.BookDeadlineRepository

class ClearBookDeadlineUseCaseTest {
    private lateinit var repository: BookDeadlineRepository
    private lateinit var useCase: ClearBookDeadlineUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = ClearBookDeadlineUseCase(repository = repository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository clearDeadline with the given bookId`() = runTest {
            // ----- Arrange -----
            val bookId = 77

            // ----- Act -----
            useCase(bookId = bookId)

            // ----- Assert -----
            coVerify {
                repository.clearDeadline(bookId = bookId)
            }
        }

        @Test
        fun `returns success when repository clearDeadline completes normally`() = runTest {
            // ----- Arrange -----
            val bookId = 3

            coEvery {
                repository.clearDeadline(bookId = bookId)
            } returns Unit

            // ----- Act -----
            val result = useCase(bookId = bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when repository clearDeadline throws`() = runTest {
            // ----- Arrange -----
            val bookId = 4
            val expectedError = RuntimeException("delete failed")

            coEvery {
                repository.clearDeadline(bookId = bookId)
            } throws expectedError

            // ----- Act -----
            val result = useCase(bookId = bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
