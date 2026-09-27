package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PersistEditionImageUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: PersistEditionImageUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = PersistEditionImageUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success and delegates to repository when call succeeds`() = runTest {
            // ----- Arrange -----
            val editionId = 42
            val url = "https://example.com/cover42.jpg"
            val bytes = byteArrayOf(1, 2, 3)

            coEvery {
                booksRepository.persistEditionImage(
                    editionId = editionId,
                    url = url,
                    bytes = bytes,
                )
            } returns Unit

            // ----- Act -----
            val result = useCase(
                editionId = editionId,
                url = url,
                bytes = bytes,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `forwards editionId, url, and bytes to the repository`() = runTest {
            // ----- Arrange -----
            val editionId = 7
            val url = "https://example.com/cover7.jpg"
            val bytes = byteArrayOf(4, 5, 6)

            coEvery {
                booksRepository.persistEditionImage(
                    editionId = editionId,
                    url = url,
                    bytes = bytes,
                )
            } returns Unit

            // ----- Act -----
            useCase(
                editionId = editionId,
                url = url,
                bytes = bytes,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRepository.persistEditionImage(
                    editionId = editionId,
                    url = url,
                    bytes = bytes,
                )
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val editionId = 5
            val url = "https://example.com/cover5.jpg"
            val bytes = byteArrayOf(7, 8, 9)
            val error = RuntimeException("disk full")

            coEvery {
                booksRepository.persistEditionImage(
                    editionId = editionId,
                    url = url,
                    bytes = bytes,
                )
            } throws error

            // ----- Act -----
            val result = useCase(
                editionId = editionId,
                url = url,
                bytes = bytes,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }
    }
}
