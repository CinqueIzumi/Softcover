package nl.rhaydus.softcover.core.lists.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetAllUserListsUseCaseTest {
    private lateinit var listsRepository: ListsRepository
    private lateinit var useCase: GetAllUserListsUseCase

    @BeforeEach
    fun setUp() {
        listsRepository = mockk()
        useCase = GetAllUserListsUseCase(listsRepository = listsRepository)
    }

    private fun stubBookList(): BookList = mockk()

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository allUserLists flow`() = runTest {
            // ----- Arrange -----
            val bookList = stubBookList()

            every {
                listsRepository.allUserLists
            } returns flowOf(listOf(bookList))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe listOf(bookList)
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when repository emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                listsRepository.allUserLists
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }
}
