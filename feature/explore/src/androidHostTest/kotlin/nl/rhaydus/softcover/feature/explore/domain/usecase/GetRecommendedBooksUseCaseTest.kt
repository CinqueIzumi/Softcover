package nl.rhaydus.softcover.feature.explore.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveRecommendedBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetRecommendedBooksUseCaseTest {
    private lateinit var recommendedFlow: MutableStateFlow<List<Book>>
    private lateinit var ownedFlow: MutableStateFlow<Set<Int>>
    private lateinit var useCase: GetRecommendedBooksUseCase

    @BeforeEach
    fun setUp() {
        recommendedFlow = MutableStateFlow(emptyList())
        ownedFlow = MutableStateFlow(emptySet())
        val observeRecommendedBooks = mockk<ObserveRecommendedBooksUseCase>()
        val observeOwnedBookIds = mockk<ObserveOwnedBookIdsUseCase>()

        every {
            observeRecommendedBooks()
        } returns recommendedFlow

        every {
            observeOwnedBookIds()
        } returns ownedFlow

        useCase = GetRecommendedBooksUseCase(
            observeRecommendedBooks = observeRecommendedBooks,
            observeOwnedBookIds = observeOwnedBookIds,
        )
    }

    private fun recommended(id: Int): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    private fun Book.idOf(): Int = id

    @Nested
    inner class Invoke {
        @Test
        fun `keeps the recommendation rank order`() = runTest {
            // ----- Arrange -----
            recommendedFlow.value = listOf(5, 1, 9).map(::recommended)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().map { it.idOf() } shouldBe listOf(5, 1, 9)
            }
        }

        @Test
        fun `drops owned books`() = runTest {
            // ----- Arrange -----
            recommendedFlow.value = listOf(1, 2, 3).map(::recommended)
            ownedFlow.value = setOf(2)

            // ----- Act & Assert -----
            useCase().test {
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
            useCase().test {
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
            useCase().test {
                awaitItem().map { it.idOf() } shouldBe listOf(1, 2, 3)

                ownedFlow.value = setOf(2)
                awaitItem().map { it.idOf() } shouldBe listOf(1, 3)
            }
        }
    }
}
