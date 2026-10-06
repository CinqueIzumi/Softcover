package nl.rhaydus.softcover.feature.lists.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
        fun `creates the list with the given name and privacy and returns it`() = runTest {
            // ----- Arrange -----
            val expected = mockk<BookList>()

            coEvery {
                listsRepository.createList(
                    name = "Favourites",
                    privacy = PrivacySetting.PRIVATE,
                )
            } returns expected

            // ----- Act -----
            val result = useCase(
                name = "Favourites",
                privacy = PrivacySetting.PRIVATE,
            )

            // ----- Assert -----
            result shouldBe expected
        }
    }
}
