package nl.rhaydus.softcover.feature.lists.domain.usecase

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository

class CreateListUseCaseTest {
    private lateinit var listsRepository: ListsRepository
    private lateinit var useCase: CreateListUseCase

    @BeforeEach
    fun setUp() {
        listsRepository = mockk()
        useCase = CreateListUseCase(listsRepository = listsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns the BookList produced by the repository`() = runTest {
            // ----- Arrange -----
            val name = "My Reading List"
            val expected = mockk<BookList>()

            coEvery {
                listsRepository.createList(name = name)
            } returns expected

            // ----- Act -----
            val result = useCase(name = name)

            // ----- Assert -----
            result shouldBe expected
        }

        @Test
        fun `delegates to repository createList with the given name`() = runTest {
            // ----- Arrange -----
            val name = "Favourites"

            coEvery {
                listsRepository.createList(name = name)
            } returns mockk()

            // ----- Act -----
            useCase(name = name)

            // ----- Assert -----
            coVerify(exactly = 1) { listsRepository.createList(name = name) }
        }

        @Test
        fun `propagates exception thrown by repository`() = runTest {
            // ----- Arrange -----
            val name = "Bad List"
            val expected = RuntimeException("network error")

            coEvery {
                listsRepository.createList(name = name)
            } throws expected

            // ----- Act & Assert -----
            val thrown = shouldThrow<RuntimeException> {
                useCase(name = name)
            }

            thrown shouldBe expected
        }
    }
}
