package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecordBookProgressUseCaseTest {

    private lateinit var markBookAsReadUseCase: MarkBookAsReadUseCase
    private lateinit var updateBookProgressUseCase: UpdateBookProgressUseCase
    private lateinit var useCase: RecordBookProgressUseCase

    @BeforeEach
    fun setUp() {
        markBookAsReadUseCase = mockk()
        updateBookProgressUseCase = mockk()

        useCase = RecordBookProgressUseCase(
            markBookAsReadUseCase = markBookAsReadUseCase,
            updateBookProgressUseCase = updateBookProgressUseCase,
        )
    }

    private fun stubBook(
        pages: Int? = 320,
        audioSeconds: Int? = null,
    ): Book = mockk<Book>().also { book ->
        val edition = mockk<BookEdition> {
            every { this@mockk.pages } returns pages
            every { this@mockk.audioSeconds } returns audioSeconds
        }

        every { book.currentEdition } returns edition
    }

    private fun stubBookWithNullEdition(): Book = mockk<Book>().also { book ->
        every { book.currentEdition } returns null
    }

    @Nested
    inner class Invoke {

        @Test
        fun `calls markBookAsRead when newPage equals edition pages`() = runTest {
            // ----- Arrange -----
            val book = stubBook(pages = 320)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            val result = useCase(book = book, newPage = 320)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) { markBookAsReadUseCase(book = book) }
            coVerify(exactly = 0) { updateBookProgressUseCase(any(), any(), any()) }
        }

        @Test
        fun `calls updateBookProgress when newPage is less than edition pages`() = runTest {
            // ----- Arrange -----
            val book = stubBook(pages = 320)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 150, newSeconds = null)
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(book = book, newPage = 150)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) { updateBookProgressUseCase(book = book, newPage = 150, newSeconds = null) }
            coVerify(exactly = 0) { markBookAsReadUseCase(any()) }
        }

        @Test
        fun `calls markBookAsRead when newSeconds is greater than or equal to audioSeconds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(pages = null, audioSeconds = 36000)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            val result = useCase(book = book, newSeconds = 36000)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) { markBookAsReadUseCase(book = book) }
            coVerify(exactly = 0) { updateBookProgressUseCase(any(), any(), any()) }
        }

        @Test
        fun `calls updateBookProgress when newSeconds is less than audioSeconds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(pages = null, audioSeconds = 36000)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = null, newSeconds = 18000)
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(book = book, newSeconds = 18000)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) { updateBookProgressUseCase(book = book, newPage = null, newSeconds = 18000) }
            coVerify(exactly = 0) { markBookAsReadUseCase(any()) }
        }

        @Test
        fun `calls updateBookProgress when currentEdition is null`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithNullEdition()

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 100, newSeconds = null)
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(book = book, newPage = 100)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) { updateBookProgressUseCase(book = book, newPage = 100, newSeconds = null) }
            coVerify(exactly = 0) { markBookAsReadUseCase(any()) }
        }

        @Test
        fun `propagates failure from markBookAsRead when book is finished`() = runTest {
            // ----- Arrange -----
            val book = stubBook(pages = 320)
            val expectedError = RuntimeException("network error")

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase(book = book, newPage = 320)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `propagates failure from updateBookProgress when book is not finished`() = runTest {
            // ----- Arrange -----
            val book = stubBook(pages = 320)
            val expectedError = RuntimeException("server error")

            coEvery {
                updateBookProgressUseCase(book = book, newPage = 50, newSeconds = null)
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase(book = book, newPage = 50)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `calls updateBookProgress when edition has null audioSeconds and newSeconds is provided`() = runTest {
            // ----- Arrange -----
            val book = stubBook(pages = null, audioSeconds = null)

            coEvery {
                updateBookProgressUseCase(book = book, newPage = null, newSeconds = 5000)
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(book = book, newSeconds = 5000)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) { updateBookProgressUseCase(book = book, newPage = null, newSeconds = 5000) }
            coVerify(exactly = 0) { markBookAsReadUseCase(any()) }
        }
    }
}
