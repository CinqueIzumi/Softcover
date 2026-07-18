package nl.rhaydus.softcover.feature.explore.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class SearchByMoodUseCaseTest {
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: SearchByMoodUseCase

    private val mood = MoodTag(
        id = 1,
        label = "Cozy",
        slug = "cozy",
        bookCount = 10,
    )

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk()
        useCase = SearchByMoodUseCase(exploreRepository = exploreRepository)
    }

    private fun stubBook(id: Int = 1): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository searchByMood with the mood`() = runTest {
            // ----- Arrange -----
            coEvery {
                exploreRepository.searchByMood(mood = mood)
            } returns Unit

            // ----- Act -----
            useCase(mood = mood)

            // ----- Assert -----
            coVerify {
                exploreRepository.searchByMood(mood = mood)
            }
        }

        @Test
        fun `returns success when the repository call succeeds`() = runTest {
            // ----- Arrange -----
            coEvery {
                exploreRepository.searchByMood(mood = mood)
            } returns Unit

            // ----- Act -----
            val result = useCase(mood = mood)

            // ----- Assert -----
            result shouldBe Result.success(Unit)
        }

        @Test
        fun `returns failure when the repository throws`() = runTest {
            // ----- Arrange -----
            val error = RuntimeException("network error")

            coEvery {
                exploreRepository.searchByMood(mood = mood)
            } throws error

            // ----- Act -----
            val result = useCase(mood = mood)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }

        @Test
        fun `writes its results through the same queriedBooks flow used by a text search`() = runTest {
            // ----- Arrange -----
            // ExploreRepository.queriedBooks is a single shared flow - both searchByMood and
            // searchForName publish into it (see the interface doc on `queriedBooks` /
            // `searchByMood`). GetQueriedBooksUseCase is a pure pass-through over that same
            // property, so observing a mood search's result through it proves the mood path
            // reuses the shared results flow rather than a parallel one.
            val moodResultBook = stubBook(id = 99)
            val queriedBooksFlow = MutableSharedFlow<List<Book>>()

            every {
                exploreRepository.queriedBooks
            } returns queriedBooksFlow

            coEvery {
                exploreRepository.searchByMood(mood = mood)
            } coAnswers {
                queriedBooksFlow.emit(listOf(moodResultBook))
            }

            val getQueriedBooksUseCase = GetQueriedBooksUseCase(searchRepository = exploreRepository)

            // ----- Act & Assert -----
            getQueriedBooksUseCase().test {
                useCase(mood = mood)

                awaitItem() shouldBe listOf(moodResultBook)
            }
        }
    }
}
