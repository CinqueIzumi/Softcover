package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.graphics.Bitmap
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
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
        position: Flow<TrendingPosition> = flowOf(positionAt(0)),
        observeTrending: (TrendingPeriod) -> Flow<List<Book>>,
    ): Flow<TrendingWidgetUiModel> = trendingWidgetModels(
        isSignedIn = flowOf(isSignedIn),
        themeConfiguration = flowOf(ThemeConfiguration()),
        position = position,
        observeTrending = observeTrending,
        eyebrowFor = { period -> "Eyebrow ${period.name}" },
        loadCover = { book ->
            loadedBookIds += book.id
            null as Bitmap?
        },
    )

    private fun positionAt(
        page: Int,
        period: TrendingPeriod = TrendingPeriod.Week,
    ): TrendingPosition = TrendingPosition(
        period = period,
        page = page,
    )

    private fun fixedBooks(books: List<Book>): (TrendingPeriod) -> Flow<List<Book>> = { flowOf(books) }

    @Nested
    inner class TrendingWidgetModels {
        @Test
        fun `signed out shows the signed out content`() = runTest {
            // ----- Arrange -----
            val flow = models(
                isSignedIn = false,
                observeTrending = fixedBooks(listOf(book(1))),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.SignedOut
                awaitComplete()
            }
        }

        @Test
        fun `an empty trending list shows the empty content with the period eyebrow`() = runTest {
            // ----- Arrange -----
            val flow = models(observeTrending = fixedBooks(emptyList()))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                awaitItem().content shouldBe TrendingWidgetUiModel.Content.Empty(eyebrow = "Eyebrow Week")
                awaitComplete()
            }
        }

        @Test
        fun `the first page carries rank, book details and the page label`() = runTest {
            // ----- Arrange -----
            val flow = models(observeTrending = fixedBooks(listOf(book(30), book(10), book(20), book(40), book(50))))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.eyebrow shouldBe "Eyebrow Week"
                content.items.map { it.rank } shouldContainExactly listOf("1", "2")
                content.items.map { it.bookId } shouldContainExactly listOf(30, 10)
                content.items.first().title shouldBe "Title 30"
                content.items.first().author shouldBe "Author 30"
                content.pageLabel shouldBe "1/3"
                content.showsArrows shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `a later page continues the ranking and labels its page`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(positionAt(1)),
                observeTrending = fixedBooks((1..10).map { book(it) }),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.rank } shouldContainExactly listOf("3", "4")
                content.items.map { it.bookId } shouldContainExactly listOf(3, 4)
                content.pageLabel shouldBe "2/5"
                awaitComplete()
            }
        }

        @Test
        fun `a single page has no label and no arrows`() = runTest {
            // ----- Arrange -----
            val flow = models(observeTrending = fixedBooks(listOf(book(1), book(2))))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.rank } shouldContainExactly listOf("1", "2")
                content.pageLabel.shouldBeNull()
                content.showsArrows shouldBe false
                awaitComplete()
            }
        }

        @Test
        fun `the list is capped at ten so the last page is the fifth`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(positionAt(4)),
                observeTrending = fixedBooks((1..15).map { book(it) }),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.bookId } shouldContainExactly listOf(9, 10)
                content.pageLabel shouldBe "5/5"
                awaitComplete()
            }
        }

        @Test
        fun `covers are loaded only for the books on the shown page`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(positionAt(2)),
                observeTrending = fixedBooks((1..8).map { book(it) }),
            )

            // ----- Act -----
            flow.test {
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            loadedBookIds.sorted() shouldContainExactly listOf(5, 6)
        }

        @Test
        fun `a stale stored page past the end is clamped to the last page`() = runTest {
            // ----- Arrange -----
            val flow = models(
                position = flowOf(positionAt(9)),
                observeTrending = fixedBooks((1..5).map { book(it) }),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
                content.items.map { it.bookId } shouldContainExactly listOf(5)
                content.pageLabel shouldBe "3/3"
                awaitComplete()
            }
        }

        @Test
        fun `switching the period resubscribes to that period's list`() = runTest {
            // ----- Arrange -----
            val position = MutableStateFlow(positionAt(0))
            val booksByPeriod = mapOf(
                TrendingPeriod.Week to listOf(book(1), book(2)),
                TrendingPeriod.Year to listOf(book(100), book(200)),
            )
            val flow = models(
                position = position,
                observeTrending = { period -> flowOf(booksByPeriod.getValue(period)) },
            )

            // ----- Act -----
            lateinit var week: TrendingWidgetUiModel.Content
            lateinit var year: TrendingWidgetUiModel.Content
            flow.test {
                week = awaitItem().content
                position.value = positionAt(
                    page = 0,
                    period = TrendingPeriod.Year,
                )
                year = awaitItem().content
                cancelAndIgnoreRemainingEvents()
            }

            // ----- Assert -----
            val weekContent = week.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
            weekContent.eyebrow shouldBe "Eyebrow Week"
            weekContent.items.map { it.bookId } shouldContainExactly listOf(1, 2)
            val yearContent = year.shouldBeInstanceOf<TrendingWidgetUiModel.Content.Ranked>()
            yearContent.eyebrow shouldBe "Eyebrow Year"
            yearContent.items.map { it.bookId } shouldContainExactly listOf(100, 200)
        }
    }
}
