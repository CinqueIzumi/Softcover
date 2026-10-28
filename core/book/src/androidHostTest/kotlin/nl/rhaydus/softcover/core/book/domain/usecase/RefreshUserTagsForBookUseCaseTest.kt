package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.UserTagsRepository
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RefreshUserTagsForBookUseCaseTest {
    private lateinit var userTagsRepository: UserTagsRepository
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var useCase: RefreshUserTagsForBookUseCase

    @BeforeEach
    fun setUp() {
        userTagsRepository = mockk(relaxed = true)
        getUserIdUseCase = mockk()
        useCase = RefreshUserTagsForBookUseCase(
            userTagsRepository = userTagsRepository,
            getUserIdUseCase = getUserIdUseCase,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `fails without fetching when the user id cannot be resolved`() = runTest {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.failure(IllegalStateException("signed out"))

            // ----- Act -----
            val result = useCase(bookId = 7)

            // ----- Assert -----
            result.exceptionOrNull().shouldBeInstanceOf<IllegalStateException>().message shouldBe "signed out"
            coVerify(exactly = 0) {
                userTagsRepository.refreshForBook(
                    userId = any(),
                    bookId = any(),
                )
            }
        }
    }
}
