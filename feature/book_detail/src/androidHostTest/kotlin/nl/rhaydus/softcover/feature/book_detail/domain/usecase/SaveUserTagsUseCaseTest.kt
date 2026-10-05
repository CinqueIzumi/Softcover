package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SaveUserTagsUseCaseTest {
    private lateinit var userTagsRepository: UserTagsRepository
    private lateinit var useCase: SaveUserTagsUseCase

    @BeforeEach
    fun setUp() {
        userTagsRepository = mockk()
        useCase = SaveUserTagsUseCase(userTagsRepository = userTagsRepository)
    }

    private fun stubUserTag(name: String = "Fantasy"): UserTag = UserTag(
        name = name,
        category = TagCategory.GENRE,
        count = 3,
        spoiler = false,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `returns success with the canonical list returned by the repository`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val inputTags = listOf(stubUserTag("Fantasy"))
            val savedTags = listOf(stubUserTag("Fantasy").copy(count = 99))

            coEvery {
                userTagsRepository.saveTags(
                    bookId = bookId,
                    tags = inputTags,
                )
            } returns savedTags

            // ----- Act -----
            val result = useCase(
                bookId = bookId,
                tags = inputTags,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe savedTags
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val tags = listOf(stubUserTag())
            val expectedError = RuntimeException("network failure")

            coEvery {
                userTagsRepository.saveTags(
                    bookId = bookId,
                    tags = tags,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                bookId = bookId,
                tags = tags,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
