package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagVocabularyRepository

class ObserveUserTagVocabularyUseCaseTest {
    private lateinit var userTagVocabularyRepository: UserTagVocabularyRepository
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var useCase: ObserveUserTagVocabularyUseCase

    @BeforeEach
    fun setUp() {
        userTagVocabularyRepository = mockk()
        getUserIdUseCase = mockk()
        useCase = ObserveUserTagVocabularyUseCase(
            userTagVocabularyRepository = userTagVocabularyRepository,
            getUserIdUseCase = getUserIdUseCase,
        )
    }

    private fun stubUserTag(name: String = "Fantasy"): UserTag = UserTag(
        name = name,
        category = TagCategory.GENRE,
        count = 2,
        spoiler = false,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `emits the repository's vocabulary for the resolved user`() = runTest {
            // ----- Arrange -----
            val userId = 7
            val tags = listOf(stubUserTag("Fantasy"), stubUserTag("Epic"))

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            every {
                userTagVocabularyRepository.observe(userId = userId)
            } returns flowOf(tags)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe tags
                awaitComplete()
            }
        }

        @Test
        fun `forwards every emission from the repository's flow`() = runTest {
            // ----- Arrange -----
            val userId = 3
            val firstEmission = listOf(stubUserTag("Fantasy"))
            val secondEmission = listOf(stubUserTag("Fantasy"), stubUserTag("Horror"))

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            every {
                userTagVocabularyRepository.observe(userId = userId)
            } returns flowOf(
                firstEmission,
                secondEmission,
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe firstEmission
                awaitItem() shouldBe secondEmission
                awaitComplete()
            }
        }

        @Test
        fun `emits an empty list without crashing when user id resolution fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.failure(RuntimeException("no user id"))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }

        @Test
        fun `emits an empty list without crashing when the repository flow throws`() = runTest {
            // ----- Arrange -----
            val userId = 5

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            every {
                userTagVocabularyRepository.observe(userId = userId)
            } returns flow { throw RuntimeException("local read failed") }

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }
}
