package nl.rhaydus.softcover.core.lists.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveSystemVibeBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetRecommendedBooksUseCaseTest {
    private lateinit var recommendedFlow: MutableStateFlow<List<Book>>
    private lateinit var ownedFlow: MutableStateFlow<Set<Int>>
    private lateinit var hiddenFlow: MutableStateFlow<List<HiddenRecommendation>>
    private lateinit var useCase: GetRecommendedBooksUseCase

    @BeforeEach
    fun setUp() {
        recommendedFlow = MutableStateFlow(emptyList())
        ownedFlow = MutableStateFlow(emptySet())
        hiddenFlow = MutableStateFlow(emptyList())
        val observeSystemVibeBooks = mockk<ObserveSystemVibeBooksUseCase>()
        val observeOwnedBookIds = mockk<ObserveOwnedBookIdsUseCase>()
        val observeHiddenRecommendations = mockk<ObserveHiddenRecommendationsUseCase>()

        every {
            observeSystemVibeBooks(vibe = SystemVibe.Recommendations)
        } returns recommendedFlow

        every {
            observeOwnedBookIds()
        } returns ownedFlow

        every {
            observeHiddenRecommendations()
        } returns hiddenFlow

        useCase = GetRecommendedBooksUseCase(
            observeSystemVibeBooks = observeSystemVibeBooks,
            observeOwnedBookIds = observeOwnedBookIds,
            observeHiddenRecommendations = observeHiddenRecommendations,
        )
    }

    private fun recommended(id: Int): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    private fun hidden(id: Int): HiddenRecommendation = HiddenRecommendation(
        bookId = id,
        title = null,
        coverUrl = null,
        authorText = null,
    )

    private fun Book.idOf(): Int = id

    @Nested
    inner class Invoke {
        @Test
        fun `keeps the recommendation rank order`() = runTest {
            // ----- Arrange -----
            recommendedFlow.value = listOf(5, 1, 9).map(::recommended)

            // ----- Act & Assert -----
            useCase(
                vibe = SystemVibe.Recommendations,
                limit = 25,
            ).test {
                awaitItem().map { it.idOf() } shouldBe listOf(5, 1, 9)
            }
        }

        @Test
        fun `drops owned books`() = runTest {
            // ----- Arrange -----
            recommendedFlow.value = listOf(1, 2, 3).map(::recommended)
            ownedFlow.value = setOf(2)

            // ----- Act & Assert -----
            useCase(
                vibe = SystemVibe.Recommendations,
                limit = 25,
            ).test {
                awaitItem().map { it.idOf() } shouldBe listOf(1, 3)
            }
        }

        @Test
        fun `caps the result after dropping owned books`() = runTest {
            // ----- Arrange -----
            val ownedIds = setOf(1, 2, 3)
            val rankedIds = (1..100).toList()
            val filteredIds = rankedIds.filterNot { it in ownedIds }
            recommendedFlow.value = rankedIds.map(::recommended)
            ownedFlow.value = ownedIds

            // ----- Act -----
            useCase(
                vibe = SystemVibe.Recommendations,
                limit = 25,
            ).test {
                val resultIds = awaitItem().map { it.idOf() }

                // ----- Assert -----
                resultIds.none { it in ownedIds } shouldBe true
                resultIds shouldBe filteredIds.take(resultIds.size)
                (resultIds.size < filteredIds.size) shouldBe true
            }
        }

        @Test
        fun `filters live when a recommended book is added to the library`() = runTest {
            // ----- Arrange -----
            recommendedFlow.value = listOf(1, 2, 3).map(::recommended)

            // ----- Act & Assert -----
            useCase(
                vibe = SystemVibe.Recommendations,
                limit = 25,
            ).test {
                awaitItem().map { it.idOf() } shouldBe listOf(1, 2, 3)

                ownedFlow.value = setOf(2)
                awaitItem().map { it.idOf() } shouldBe listOf(1, 3)
            }
        }
    }

    @Nested
    inner class HiddenBooks {
        @Test
        fun `drops hidden books and the next ranked book fills the limit`() = runTest {
            // ----- Arrange -----
            recommendedFlow.value = listOf(1, 2, 3, 4).map(::recommended)
            hiddenFlow.value = listOf(hidden(id = 2))

            // ----- Act & Assert -----
            useCase(
                vibe = SystemVibe.Recommendations,
                limit = 3,
            ).test {
                awaitItem().map { it.idOf() } shouldBe listOf(1, 3, 4)
            }
        }

        @Test
        fun `hiding and restoring a book re-emits with the book back at its rank`() = runTest {
            // ----- Arrange -----
            recommendedFlow.value = listOf(1, 2, 3).map(::recommended)

            // ----- Act & Assert -----
            useCase(
                vibe = SystemVibe.Recommendations,
                limit = 25,
            ).test {
                awaitItem().map { it.idOf() } shouldBe listOf(1, 2, 3)

                hiddenFlow.value = listOf(hidden(id = 2))
                awaitItem().map { it.idOf() } shouldBe listOf(1, 3)

                hiddenFlow.value = emptyList()
                awaitItem().map { it.idOf() } shouldBe listOf(1, 2, 3)
            }
        }

        @Test
        fun `a book both owned and hidden is dropped once and the order holds`() = runTest {
            // ----- Arrange -----
            recommendedFlow.value = listOf(4, 2, 3, 1).map(::recommended)
            ownedFlow.value = setOf(2)
            hiddenFlow.value = listOf(hidden(id = 2), hidden(id = 1))

            // ----- Act & Assert -----
            useCase(
                vibe = SystemVibe.Recommendations,
                limit = 25,
            ).test {
                awaitItem().map { it.idOf() } shouldBe listOf(4, 3)
            }
        }
    }
}
