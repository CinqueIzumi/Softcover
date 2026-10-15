package nl.rhaydus.softcover.feature.widget.trending

import android.graphics.Bitmap
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingWidgetModelsTest {
    private val loadedBookIds = mutableListOf<Int>()

    private fun book(id: Int): Book = mockk<Book> {
        every {
            this@mockk.id
        } returns id
        every {
            title
        } returns "Title $id"
        every {
            authorString
        } returns "Author $id"
    }

    private fun models(
        isSignedIn: Boolean = true,
        books: List<Book>,
    ): Flow<TrendingWidgetUiModel> = trendingWidgetModels(
        isSignedIn = flowOf(isSignedIn),
        themeConfiguration = flowOf(ThemeConfiguration()),
        trending = flowOf(books),
        loadCover = { book ->
            loadedBookIds += book.id
            null as Bitmap?
        },
    )

    @Nested
    inner class TrendingWidgetModels {
        @Test
        fun `signed out shows the signed out content`() = runTest {
            // ----- Arrange -----
            val flow = models(
                isSignedIn = false,
                books = listOf(book(1)),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.SignedOut
                awaitComplete()
            }
        }

        @Test
        fun `an empty trending list shows the empty content`() = runTest {
            // ----- Arrange -----
            val flow = models(books = emptyList())

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.Empty
                awaitComplete()
            }
        }

        @Test
        fun `rank labels follow the list order`() = runTest {
            // ----- Arrange -----
            val flow = models(books = listOf(book(30), book(10), book(20)))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.rank } shouldContainExactly listOf("1", "2", "3")
                content.items.map { it.bookId } shouldContainExactly listOf(30, 10, 20)
                content.items.first().title shouldBe "Title 30"
                content.items.first().author shouldBe "Author 30"
                awaitComplete()
            }
        }

        @Test
        fun `the list is capped at five and only those covers are requested`() = runTest {
            // ----- Arrange -----
            val flow = models(books = (1..8).map { book(it) })

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.bookId } shouldContainExactly listOf(1, 2, 3, 4, 5)
                awaitComplete()
            }

            loadedBookIds.sorted() shouldContainExactly listOf(1, 2, 3, 4, 5)
        }
    }
}
