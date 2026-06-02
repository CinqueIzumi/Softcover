package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.File
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
            val source: File = mockk()

            coEvery {
                booksRepository.persistEditionImage(
                    editionId = editionId,
                    source = source,
                )
            } returns Unit

            // ----- Act -----
            val result = useCase(
                editionId = editionId,
                source = source,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `forwards editionId and source to the repository`() = runTest {
            // ----- Arrange -----
            val editionId = 7
            val source: File = mockk()

            coEvery {
                booksRepository.persistEditionImage(
                    editionId = editionId,
                    source = source,
                )
            } returns Unit

            // ----- Act -----
            useCase(
                editionId = editionId,
                source = source,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRepository.persistEditionImage(
                    editionId = editionId,
                    source = source,
                )
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val editionId = 5
            val source: File = mockk()
            val error = RuntimeException("disk full")

            coEvery {
                booksRepository.persistEditionImage(
                    editionId = editionId,
                    source = source,
                )
            } throws error

            // ----- Act -----
            val result = useCase(
                editionId = editionId,
                source = source,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }
    }
}
