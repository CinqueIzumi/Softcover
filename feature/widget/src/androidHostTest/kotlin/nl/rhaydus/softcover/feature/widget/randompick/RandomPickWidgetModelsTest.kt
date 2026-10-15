package nl.rhaydus.softcover.feature.widget.randompick

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
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RandomPickWidgetModelsTest {
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

    private fun list(
        id: Int,
        name: String,
    ): BookList = BookList(
        id = id,
        name = name,
        slug = "list-$id",
        books = emptyList(),
    )

    private fun models(
        isSignedIn: Boolean = true,
        resolution: RandomPickResolution,
    ): Flow<RandomPickWidgetUiModel> = randomPickWidgetModels(
        isSignedIn = flowOf(isSignedIn),
        themeConfiguration = flowOf(ThemeConfiguration()),
        wantToRead = flowOf(emptyList()),
        lists = flowOf(emptyList()),
        changes = flowOf(Unit),
        resolve = { _, _ -> resolution },
        loadCover = { book ->
            loadedBookIds += book.id
            null as Bitmap?
        },
        renderArtwork = { null },
    )

    @Nested
    inner class EyebrowFor {
        @Test
        fun `labels each source in upper case`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    id = 5,
                    name = "Beach reads",
                ),
            )

            // ----- Act -----
            val labels = listOf(
                RandomPickSource.WantToRead,
                RandomPickSource.Owned,
                RandomPickSource.UserList(listId = 5),
            ).map {
                eyebrowFor(
                    source = it,
                    lists = lists,
                )
            }

            // ----- Assert -----
            labels shouldContainExactly listOf(
                "WHAT NEXT? · WANT TO READ",
                "WHAT NEXT? · OWNED",
                "WHAT NEXT? · BEACH READS",
            )
        }
    }

    @Nested
    inner class RandomPickWidgetModels {
        @Test
        fun `signed out shows the signed out content`() = runTest {
            // ----- Arrange -----
            val flow = models(
                isSignedIn = false,
                resolution = RandomPickResolution(
                    book = book(1),
                    source = RandomPickSource.WantToRead,
                ),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                awaitItem().content shouldBe RandomPickWidgetUiModel.Content.SignedOut
                awaitComplete()
            }
        }

        @Test
        fun `an empty pool shows the empty pool content with the source eyebrow`() = runTest {
            // ----- Arrange -----
            val flow = models(
                resolution = RandomPickResolution(
                    book = null,
                    source = RandomPickSource.Owned,
                ),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content
                    .shouldBeInstanceOf<RandomPickWidgetUiModel.Content.EmptyPool>()
                content.eyebrow shouldBe "WHAT NEXT? · OWNED"
                awaitComplete()
            }

            loadedBookIds shouldContainExactly emptyList()
        }

        @Test
        fun `a picked book shows its details and loads only its cover`() = runTest {
            // ----- Arrange -----
            val flow = models(
                resolution = RandomPickResolution(
                    book = book(3),
                    source = RandomPickSource.WantToRead,
                ),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content
                    .shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()
                content.bookId shouldBe 3
                content.title shouldBe "Title 3"
                content.author shouldBe "Author 3"
                content.eyebrow shouldBe "WHAT NEXT? · WANT TO READ"
                awaitComplete()
            }

            loadedBookIds shouldContainExactly listOf(3)
        }
    }
}
