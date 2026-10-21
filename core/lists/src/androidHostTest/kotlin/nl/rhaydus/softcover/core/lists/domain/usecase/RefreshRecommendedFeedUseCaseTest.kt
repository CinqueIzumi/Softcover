package nl.rhaydus.softcover.core.lists.domain.usecase

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshSystemVibeBooksUseCase
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours

class RefreshRecommendedFeedUseCaseTest {
    private lateinit var refreshSystemVibeBooks: RefreshSystemVibeBooksUseCase
    private lateinit var useCase: RefreshRecommendedFeedUseCase

    @BeforeEach
    fun setUp() {
        refreshSystemVibeBooks = mockk()
        useCase = RefreshRecommendedFeedUseCase(refreshSystemVibeBooks = refreshSystemVibeBooks)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `succeeds when every vibe refreshes`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshSystemVibeBooks(
                    vibe = any(),
                    maxAge = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(maxAge = 1.hours)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `refreshes every vibe`() = runTest {
            // ----- Arrange -----
            val refreshed = mutableListOf<SystemVibe>()

            coEvery {
                refreshSystemVibeBooks(
                    vibe = any(),
                    maxAge = any(),
                )
            } coAnswers {
                refreshed += firstArg<SystemVibe>()
                Result.success(Unit)
            }

            // ----- Act -----
            useCase(maxAge = 1.hours)

            // ----- Assert -----
            refreshed shouldContainExactlyInAnyOrder SystemVibe.entries
        }

        @Test
        fun `fails when one vibe fails and still refreshes the other`() = runTest {
            // ----- Arrange -----
            val failure = IllegalStateException("offline")

            coEvery {
                refreshSystemVibeBooks(
                    vibe = SystemVibe.Recommendations,
                    maxAge = any(),
                )
            } returns Result.failure(failure)

            coEvery {
                refreshSystemVibeBooks(
                    vibe = SystemVibe.TopPicks,
                    maxAge = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(maxAge = 1.hours)

            // ----- Assert -----
            result.exceptionOrNull() shouldBe failure
            coVerify(exactly = 1) {
                refreshSystemVibeBooks(
                    vibe = SystemVibe.TopPicks,
                    maxAge = any(),
                )
            }
        }
    }
}
