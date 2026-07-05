package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetUserTagsUseCaseTest {
    private lateinit var userTagsRepository: UserTagsRepository
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var useCase: GetUserTagsUseCase

    @BeforeEach
    fun setUp() {
        userTagsRepository = mockk()
        getUserIdUseCase = mockk()
        useCase = GetUserTagsUseCase(
            userTagsRepository = userTagsRepository,
            getUserIdUseCase = getUserIdUseCase,
        )
    }

    private fun stubUserTag(name: String = "Fantasy"): UserTag = UserTag(
        name = name,
        category = TagCategory.GENRE,
        count = 5,
        spoiler = false,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `returns success with the list from the repository`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val userId = 7
            val expectedTags = listOf(stubUserTag("Fantasy"), stubUserTag("Epic"))

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagsRepository.getUserTags(
                    userId = userId,
                    bookId = bookId,
                )
            } returns expectedTags

            // ----- Act -----
            val result = useCase(bookId = bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe expectedTags
        }

        @Test
        fun `returns success with empty list when repository returns no tags`() = runTest {
            // ----- Arrange -----
            val bookId = 10
            val userId = 3

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagsRepository.getUserTags(
                    userId = userId,
                    bookId = bookId,
                )
            } returns emptyList()

            // ----- Act -----
            val result = useCase(bookId = bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe emptyList()
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val userId = 2
            val expectedError = RuntimeException("network failure")

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagsRepository.getUserTags(
                    userId = userId,
                    bookId = bookId,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(bookId = bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure and never calls repository when GetUserIdUseCase returns failure`() = runTest {
            // ----- Arrange -----
            val bookId = 99
            val idError = RuntimeException("no user id")

            coEvery {
                getUserIdUseCase()
            } returns Result.failure(idError)

            // ----- Act -----
            val result = useCase(bookId = bookId)

            // ----- Assert -----
            result.isFailure shouldBe true

            coVerify(exactly = 0) {
                userTagsRepository.getUserTags(
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `wraps the exception in the Result rather than propagating it`() = runTest {
            // ----- Arrange -----
            val bookId = 3
            val userId = 1

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagsRepository.getUserTags(
                    userId = userId,
                    bookId = bookId,
                )
            } throws IllegalStateException("server error")

            // ----- Act -----
            val result = runCatching { useCase(bookId = bookId) }

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull()!!.isFailure shouldBe true
        }
    }
}
