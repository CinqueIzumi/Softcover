package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.baseBook
import nl.rhaydus.softcover.core.book.domain.model.IsbnEditionMatch
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ResolveBookByIsbnUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var fetchBookByIdUseCase: FetchBookByIdUseCase
    private lateinit var useCase: ResolveBookByIsbnUseCase

    private val validIsbn = "9780374710781"
    private val bookId = 42
    private val editionId = 99
    private val book = baseBook

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        fetchBookByIdUseCase = mockk()
        useCase = ResolveBookByIsbnUseCase(
            booksRepository = booksRepository,
            fetchBookByIdUseCase = fetchBookByIdUseCase,
        )
    }

    @Nested
    inner class Invoke {

        @Test
        fun `valid ISBN with matching bookId returns Found with hydrated book`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.fetchEditionMatchForIsbn(isbn = validIsbn)
            } returns IsbnEditionMatch(bookId = bookId, editionId = editionId)

            coEvery { fetchBookByIdUseCase(id = bookId) } returns Result.success(book)

            // ----- Act -----
            val result = useCase(validIsbn)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe IsbnLookupResult.Found(book = book, editionId = editionId)

            coVerify(exactly = 1) { booksRepository.fetchEditionMatchForIsbn(isbn = validIsbn) }
            coVerify(exactly = 1) { fetchBookByIdUseCase(id = bookId) }
        }

        @Test
        fun `valid ISBN with no matching bookId returns UnknownEdition without calling fetchBookByIdUseCase`() = runTest {
            // ----- Arrange -----
            coEvery { booksRepository.fetchEditionMatchForIsbn(isbn = validIsbn) } returns null

            // ----- Act -----
            val result = useCase(validIsbn)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe IsbnLookupResult.UnknownEdition(normalizedIsbn = validIsbn)

            coVerify(exactly = 0) { fetchBookByIdUseCase(id = any()) }
        }

        @Test
        fun `malformed ISBN returns InvalidIsbn without calling repository`() = runTest {
            // ----- Arrange -----
            // ----- Act -----
            val result = useCase("not-an-isbn")

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe IsbnLookupResult.InvalidIsbn

            coVerify(exactly = 0) { booksRepository.fetchEditionMatchForIsbn(isbn = any()) }
        }

        @Test
        fun `repository throws returns failure`() = runTest {
            // ----- Arrange -----
            coEvery { booksRepository.fetchEditionMatchForIsbn(isbn = validIsbn) } throws OfflineException()

            // ----- Act -----
            val result = useCase(validIsbn)

            // ----- Assert -----
            result.isFailure shouldBe true
        }

        @Test
        fun `fetchBookByIdUseCase returns failure causes overall failure via getOrThrow`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.fetchEditionMatchForIsbn(isbn = validIsbn)
            } returns IsbnEditionMatch(bookId = bookId, editionId = editionId)

            coEvery { fetchBookByIdUseCase(id = bookId) } returns Result.failure(RuntimeException("hydration failed"))

            // ----- Act -----
            val result = useCase(validIsbn)

            // ----- Assert -----
            result.isFailure shouldBe true
        }
    }
}
