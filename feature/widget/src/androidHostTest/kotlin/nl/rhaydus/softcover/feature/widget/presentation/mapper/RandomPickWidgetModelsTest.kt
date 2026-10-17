package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.net.Uri
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickResolution
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class RandomPickWidgetModelsTest {
    private val requestedBookIds = mutableListOf<Int>()

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

    private val uri = mockk<Uri>()

    private val artwork = RandomPickWidgetUiModel.Artwork(
        backdrop = null,
        veil = uri,
        veilLift = uri,
        washPrimary = uri,
        washTertiary = uri,
    )

    private fun models(
        isSignedIn: Boolean = true,
        resolution: Result<RandomPickResolution>,
        coverUri: suspend (Book) -> Uri? = { null },
        artwork: suspend (Book) -> RandomPickWidgetUiModel.Artwork? = { null },
    ): Flow<RandomPickWidgetUiModel> = randomPickWidgetModels(
        isSignedIn = flowOf(isSignedIn),
        themeConfiguration = flowOf(ThemeConfiguration()),
        wantToRead = flowOf(emptyList()),
        lists = flowOf(emptyList()),
        changes = flowOf(Unit),
        resolve = { _, _ -> resolution },
        coverUri = {
            requestedBookIds += it.id
            coverUri(it)
        },
        artwork = artwork,
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
                resolution = Result.success(
                    RandomPickResolution.Resolved(
                        book = book(1),
                        source = RandomPickSource.WantToRead,
                    ),
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
        fun `an unavailable source shows the source unavailable content`() = runTest {
            // ----- Arrange -----
            val flow = models(resolution = Result.success(RandomPickResolution.SourceUnavailable))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                awaitItem().content shouldBe RandomPickWidgetUiModel.Content.SourceUnavailable
                awaitComplete()
            }
        }

        @Test
        fun `an empty pool shows the empty pool content with the source eyebrow`() = runTest {
            // ----- Arrange -----
            val flow = models(
                resolution = Result.success(
                    RandomPickResolution.Resolved(
                        book = null,
                        source = RandomPickSource.Owned,
                    ),
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

            requestedBookIds shouldContainExactly emptyList()
        }

        @Test
        fun `a failed resolve emits nothing`() = runTest {
            // ----- Arrange -----
            val flow = models(resolution = Result.failure(IllegalStateException("boom")))

            // ----- Act & Assert -----
            flow.test {
                awaitComplete()
            }
        }

        @Test
        fun `images that resolve at once skip the pending step`() = runTest {
            // ----- Arrange -----
            val flow = models(
                resolution = Result.success(
                    RandomPickResolution.Resolved(
                        book = book(3),
                        source = RandomPickSource.WantToRead,
                    ),
                ),
                coverUri = { uri },
                artwork = { artwork },
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
                content.cover shouldBe RandomPickWidgetUiModel.Cover.Loaded(uri = uri)
                content.artwork shouldBe artwork
                awaitComplete()
            }

            requestedBookIds shouldContainExactly listOf(3)
        }

        @Test
        fun `slow images emit the text first with a pending cover then the finished model`() = runTest {
            // ----- Arrange -----
            val flow = models(
                resolution = Result.success(
                    RandomPickResolution.Resolved(
                        book = book(3),
                        source = RandomPickSource.WantToRead,
                    ),
                ),
                coverUri = {
                    delay(1.seconds)
                    uri
                },
                artwork = { artwork },
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val first = awaitItem().content
                    .shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()
                first.title shouldBe "Title 3"
                first.cover shouldBe RandomPickWidgetUiModel.Cover.Pending
                first.artwork.shouldBeNull()

                val second = awaitItem().content
                    .shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()
                second.cover shouldBe RandomPickWidgetUiModel.Cover.Loaded(uri = uri)
                second.artwork shouldBe artwork
                awaitComplete()
            }
        }

        @Test
        fun `a book without a cover file shows the missing cover`() = runTest {
            // ----- Arrange -----
            val flow = models(
                resolution = Result.success(
                    RandomPickResolution.Resolved(
                        book = book(3),
                        source = RandomPickSource.WantToRead,
                    ),
                ),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val content = awaitItem().content
                    .shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()
                content.cover shouldBe RandomPickWidgetUiModel.Cover.Missing
                awaitComplete()
            }
        }
    }
}
