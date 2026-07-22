package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagVocabularyRepository

class SyncUserTagVocabularyUseCaseTest {
    private lateinit var userTagVocabularyRepository: UserTagVocabularyRepository
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var useCase: SyncUserTagVocabularyUseCase

    @BeforeEach
    fun setUp() {
        userTagVocabularyRepository = mockk()
        getUserIdUseCase = mockk()
        useCase = SyncUserTagVocabularyUseCase(
            userTagVocabularyRepository = userTagVocabularyRepository,
            getUserIdUseCase = getUserIdUseCase,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `resolves the user id and syncs the repository from remote`() = runTest {
            // ----- Arrange -----
            val userId = 7

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagVocabularyRepository.syncFromRemote(userId = userId)
            } returns Unit

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                userTagVocabularyRepository.syncFromRemote(userId = userId)
            }
        }

        @Test
        fun `returns failure and never calls the repository when GetUserIdUseCase returns failure`() = runTest {
            // ----- Arrange -----
            val idError = RuntimeException("no user id")

            coEvery {
                getUserIdUseCase()
            } returns Result.failure(idError)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true

            coVerify(exactly = 0) {
                userTagVocabularyRepository.syncFromRemote(any())
            }
        }

        @Test
        fun `returns failure when the repository throws`() = runTest {
            // ----- Arrange -----
            val userId = 3
            val expectedError = RuntimeException("network failure")

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagVocabularyRepository.syncFromRemote(userId = userId)
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `wraps the exception in the Result rather than propagating it`() = runTest {
            // ----- Arrange -----
            val userId = 1

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                userTagVocabularyRepository.syncFromRemote(userId = userId)
            } throws IllegalStateException("server error")

            // ----- Act -----
            val result = runCatching { useCase() }

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull()!!.isFailure shouldBe true
        }
    }
}
