package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetEditionAsOwnedUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: SetEditionAsOwnedUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = SetEditionAsOwnedUseCase(booksRepository = booksRepository)
    }

    private fun stubEdition(id: Int): BookEdition = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class Invoke {

        @Test
        fun `when owned is true marks edition as owned and caches the list book`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(id = 10)
            val listBook = mockk<ListBook>()

            coEvery {
                booksRepository.markEditionAsOwned(edition = edition)
            } returns listBook

            coJustRun {
                booksRepository.cacheListBook(book = listBook)
            }

            // ----- Act -----
            val result = useCase(
                edition = edition,
                owned = true,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { booksRepository.markEditionAsOwned(edition = edition) }
            coVerify(exactly = 1) { booksRepository.cacheListBook(book = listBook) }
            coVerify(exactly = 0) { booksRepository.getListBookByEditionId(editionId = any()) }
            coVerify(exactly = 0) { booksRepository.removeListBook(book = any()) }
        }

        @Test
        fun `when owned is false removes the list book looked up by edition id`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val edition = stubEdition(id = editionId)
            val listBook = mockk<ListBook>()

            coEvery {
                booksRepository.getListBookByEditionId(editionId = editionId)
            } returns listBook

            coJustRun {
                booksRepository.removeListBook(book = listBook)
            }

            // ----- Act -----
            val result = useCase(
                edition = edition,
                owned = false,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { booksRepository.getListBookByEditionId(editionId = editionId) }
            coVerify(exactly = 1) { booksRepository.removeListBook(book = listBook) }
            coVerify(exactly = 0) { booksRepository.markEditionAsOwned(edition = any()) }
            coVerify(exactly = 0) { booksRepository.cacheListBook(book = any()) }
        }

        @Test
        fun `returns failure when markEditionAsOwned throws`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(id = 10)
            val expectedError = RuntimeException("network error")

            coEvery {
                booksRepository.markEditionAsOwned(edition = edition)
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                edition = edition,
                owned = true,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) { booksRepository.cacheListBook(book = any()) }
        }

        @Test
        fun `returns failure when getListBookByEditionId throws`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val edition = stubEdition(id = editionId)
            val expectedError = RuntimeException("db error")

            coEvery {
                booksRepository.getListBookByEditionId(editionId = editionId)
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                edition = edition,
                owned = false,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) { booksRepository.removeListBook(book = any()) }
        }
    }
}
