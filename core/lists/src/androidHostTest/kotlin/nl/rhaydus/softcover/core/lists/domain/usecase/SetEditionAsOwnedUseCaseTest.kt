package nl.rhaydus.softcover.core.lists.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetEditionAsOwnedUseCaseTest {
    private lateinit var listsRepository: ListsRepository
    private lateinit var useCase: SetEditionAsOwnedUseCase

    @BeforeEach
    fun setUp() {
        listsRepository = mockk()
        useCase = SetEditionAsOwnedUseCase(listsRepository = listsRepository)
    }

    private fun stubEdition(id: Int): BookEdition = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class Invoke {
        @Test
        fun `when owned is true calls markEditionAsOwned and nothing else`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(id = 10)

            coJustRun {
                listsRepository.markEditionAsOwned(edition = edition)
            }

            // ----- Act -----
            val result = useCase(
                edition = edition,
                owned = true,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) { listsRepository.markEditionAsOwned(edition = edition) }

            coVerify(exactly = 0) { listsRepository.removeOwnedEdition(editionId = any()) }
        }

        @Test
        fun `when owned is false calls removeOwnedEdition with edition id and nothing else`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val edition = stubEdition(id = editionId)

            coJustRun {
                listsRepository.removeOwnedEdition(editionId = editionId)
            }

            // ----- Act -----
            val result = useCase(
                edition = edition,
                owned = false,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) { listsRepository.removeOwnedEdition(editionId = editionId) }

            coVerify(exactly = 0) { listsRepository.markEditionAsOwned(edition = any()) }
        }

        @Test
        fun `returns failure when markEditionAsOwned throws`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(id = 10)
            val expectedError = RuntimeException("network error")

            coEvery {
                listsRepository.markEditionAsOwned(edition = edition)
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                edition = edition,
                owned = true,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure when removeOwnedEdition throws`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val edition = stubEdition(id = editionId)
            val expectedError = RuntimeException("db error")

            coEvery {
                listsRepository.removeOwnedEdition(editionId = editionId)
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                edition = edition,
                owned = false,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
