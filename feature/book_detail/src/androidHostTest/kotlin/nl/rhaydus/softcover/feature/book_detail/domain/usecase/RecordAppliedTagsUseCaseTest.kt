package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagVocabularyRepository

class RecordAppliedTagsUseCaseTest {
    private lateinit var userTagVocabularyRepository: UserTagVocabularyRepository
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var useCase: RecordAppliedTagsUseCase

    @BeforeEach
    fun setUp() {
        userTagVocabularyRepository = mockk()
        getUserIdUseCase = mockk()
        useCase = RecordAppliedTagsUseCase(
            userTagVocabularyRepository = userTagVocabularyRepository,
            getUserIdUseCase = getUserIdUseCase,
        )
    }

    private fun stubUserTag(name: String = "Fantasy"): UserTag = UserTag(
        name = name,
        category = TagCategory.GENRE,
        count = 1,
        spoiler = false,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `resolves the user id and records the given tags`() = runTest {
            // ----- Arrange -----
            val userId = 7
            val tags = listOf(stubUserTag("Fantasy"), stubUserTag("Epic"))

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagVocabularyRepository.record(
                    userId = userId,
                    tags = tags,
                )
            } returns Unit

            // ----- Act -----
            val result = useCase(tags = tags)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                userTagVocabularyRepository.record(
                    userId = userId,
                    tags = tags,
                )
            }
        }

        @Test
        fun `records an empty tag list without error`() = runTest {
            // ----- Arrange -----
            val userId = 4

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagVocabularyRepository.record(
                    userId = userId,
                    tags = emptyList(),
                )
            } returns Unit

            // ----- Act -----
            val result = useCase(tags = emptyList())

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure and never calls the repository when GetUserIdUseCase returns failure`() = runTest {
            // ----- Arrange -----
            val idError = RuntimeException("no user id")
            val tags = listOf(stubUserTag())

            coEvery {
                getUserIdUseCase()
            } returns Result.failure(idError)

            // ----- Act -----
            val result = useCase(tags = tags)

            // ----- Assert -----
            result.isFailure shouldBe true

            coVerify(exactly = 0) {
                userTagVocabularyRepository.record(
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `returns failure when the repository throws`() = runTest {
            // ----- Arrange -----
            val userId = 2
            val tags = listOf(stubUserTag())
            val expectedError = RuntimeException("network failure")

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagVocabularyRepository.record(
                    userId = userId,
                    tags = tags,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(tags = tags)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `wraps the exception in the Result rather than propagating it`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val tags = listOf(stubUserTag())

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagVocabularyRepository.record(
                    userId = userId,
                    tags = tags,
                )
            } throws IllegalStateException("server error")

            // ----- Act -----
            val result = runCatching { useCase(tags = tags) }

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull()!!.isFailure shouldBe true
        }
    }
}
