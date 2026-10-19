package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.net.Uri
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickResolution
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettings
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettingsChange
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.softcover.feature.widget.presentation.model.BookArtworkUiModel
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class RandomPickWidgetModelsTest {
    private val requestedBookIds = mutableListOf<Int>()
    private val requestedHeights = mutableListOf<Int>()

    private fun book(id: Int): Book = mockk<Book> {
        every {
            this@mockk.id
        } returns id
        every {
            displayTitle
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

    private val artwork = BookArtworkUiModel(
        backdrop = null,
        veil = uri,
        veilLift = uri,
        washPrimary = uri,
        washTertiary = uri,
    )

    private fun models(
        isSignedIn: Boolean = true,
        resolution: Result<RandomPickResolution>,
        wantToRead: Flow<List<Book>> = flowOf(emptyList()),
        changes: Flow<Unit> = flowOf(Unit),
        settingsChanges: Flow<RandomPickSettingsChange> = emptyFlow(),
        coverHeightDp: Flow<Int> = flowOf(200),
        coverUri: suspend (Book) -> Uri? = { null },
        artwork: suspend (Book) -> BookArtworkUiModel? = { null },
    ): Flow<RandomPickWidgetUiModel> = randomPickWidgetModels(
        isSignedIn = flowOf(isSignedIn),
        themeConfiguration = flowOf(ThemeConfiguration()),
        wantToRead = wantToRead,
        lists = flowOf(emptyList()),
        changes = changes,
        settingsChanges = settingsChanges,
        resolve = { _, _ -> resolution },
        coverUri = { book, heightDp ->
            requestedBookIds += book.id
            requestedHeights += heightDp
            coverUri(book)
        },
        coverHeightDp = coverHeightDp,
        artwork = { book -> artwork(book) },
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

        @Test
        fun `a new cover height produces the cover again at that height and emits it`() = runTest {
            // ----- Arrange -----
            val heights = MutableStateFlow(200)
            val flow = models(
                resolution = Result.success(
                    RandomPickResolution.Resolved(
                        book = book(3),
                        source = RandomPickSource.WantToRead,
                    ),
                ),
                coverHeightDp = heights,
                coverUri = { uri },
            )

            flow.test {
                awaitItem()

                // ----- Act -----
                heights.value = 320
                val content = awaitItem().content
                    .shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()

                // ----- Assert -----
                content.cover shouldBe RandomPickWidgetUiModel.Cover.Loaded(uri = uri)
                requestedHeights shouldContainExactly listOf(200, 320)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `a height change does not emit the pending cover again`() = runTest {
            // ----- Arrange -----
            val heights = MutableStateFlow(200)
            val flow = models(
                resolution = Result.success(
                    RandomPickResolution.Resolved(
                        book = book(3),
                        source = RandomPickSource.WantToRead,
                    ),
                ),
                coverHeightDp = heights,
                coverUri = {
                    delay(1.seconds)
                    uri
                },
            )

            flow.test {
                awaitItem().content
                    .shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()
                    .cover shouldBe RandomPickWidgetUiModel.Cover.Pending
                awaitItem()

                // ----- Act -----
                heights.value = 320
                val content = awaitItem().content
                    .shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()

                // ----- Assert -----
                content.cover shouldBe RandomPickWidgetUiModel.Cover.Loaded(uri = uri)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `a cover height that never arrives still shows the book with a pending cover`() = runTest {
            // ----- Arrange -----
            val flow = models(
                resolution = Result.success(
                    RandomPickResolution.Resolved(
                        book = book(3),
                        source = RandomPickSource.WantToRead,
                    ),
                ),
                coverHeightDp = flow { awaitCancellation() },
            )

            // ----- Act -----
            flow.test {
                val content = awaitItem().content
                    .shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()

                // ----- Assert -----
                content.cover shouldBe RandomPickWidgetUiModel.Cover.Pending
                requestedHeights.shouldBeEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class SettingsChanges {
        private fun changeTo(source: RandomPickSource) = RandomPickSettingsChange(
            appWidgetId = 1,
            settings = RandomPickSettings(source = source),
        )

        private fun resolved(source: RandomPickSource) = Result.success(
            RandomPickResolution.Resolved(
                book = book(3),
                source = source,
            ),
        )

        @Test
        fun `a settings change shows the new eyebrow as switching before the resolved content`() = runTest {
            // ----- Arrange -----
            val settingsChanges = MutableSharedFlow<RandomPickSettingsChange>(replay = 1)
            settingsChanges.tryEmit(changeTo(RandomPickSource.Owned))

            val flow = models(
                resolution = resolved(RandomPickSource.Owned),
                settingsChanges = settingsChanges,
            )

            // ----- Act & Assert -----
            flow.test {
                awaitItem().content shouldBe
                    RandomPickWidgetUiModel.Content.Switching(eyebrow = "WHAT NEXT? · OWNED")

                val picked = awaitItem().content
                    .shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()
                picked.eyebrow shouldBe "WHAT NEXT? · OWNED"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `a shuffle after a settings change does not show switching again`() = runTest {
            // ----- Arrange -----
            val settingsChanges = MutableSharedFlow<RandomPickSettingsChange>(replay = 1)
            settingsChanges.tryEmit(changeTo(RandomPickSource.Owned))
            val shuffles = MutableSharedFlow<Unit>(replay = 1)
            shuffles.tryEmit(Unit)

            val flow = models(
                resolution = resolved(RandomPickSource.Owned),
                changes = shuffles,
                settingsChanges = settingsChanges,
            )

            // ----- Act -----
            val contents = mutableListOf<RandomPickWidgetUiModel.Content>()
            flow.test {
                contents += awaitItem().content
                contents += awaitItem().content
                shuffles.emit(Unit)
                contents += awaitItem().content
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            // ----- Assert -----
            contents.map { it::class } shouldContainExactly listOf(
                RandomPickWidgetUiModel.Content.Switching::class,
                RandomPickWidgetUiModel.Content.Picked::class,
                RandomPickWidgetUiModel.Content.Picked::class,
            )
        }

        @Test
        fun `a data refresh after a settings change does not show switching again`() = runTest {
            // ----- Arrange -----
            val settingsChanges = MutableSharedFlow<RandomPickSettingsChange>(replay = 1)
            settingsChanges.tryEmit(changeTo(RandomPickSource.Owned))
            val books = MutableStateFlow(emptyList<Book>())

            val flow = models(
                resolution = resolved(RandomPickSource.Owned),
                wantToRead = books,
                settingsChanges = settingsChanges,
            )

            // ----- Act -----
            val contents = mutableListOf<RandomPickWidgetUiModel.Content>()
            flow.test {
                contents += awaitItem().content
                contents += awaitItem().content
                books.value = listOf(book(9))
                contents += awaitItem().content
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            // ----- Assert -----
            contents.map { it::class } shouldContainExactly listOf(
                RandomPickWidgetUiModel.Content.Switching::class,
                RandomPickWidgetUiModel.Content.Picked::class,
                RandomPickWidgetUiModel.Content.Picked::class,
            )
        }

        @Test
        fun `a second settings change shows switching again with its own eyebrow`() = runTest {
            // ----- Arrange -----
            val settingsChanges = MutableSharedFlow<RandomPickSettingsChange>(replay = 1)
            settingsChanges.tryEmit(changeTo(RandomPickSource.Owned))
            val shuffles = MutableSharedFlow<Unit>(replay = 1)
            shuffles.tryEmit(Unit)

            val flow = models(
                resolution = resolved(RandomPickSource.WantToRead),
                changes = shuffles,
                settingsChanges = settingsChanges,
            )

            // ----- Act -----
            val contents = mutableListOf<RandomPickWidgetUiModel.Content>()
            flow.test {
                contents += awaitItem().content
                contents += awaitItem().content
                settingsChanges.emit(changeTo(RandomPickSource.WantToRead))
                shuffles.emit(Unit)
                contents += awaitItem().content
                contents += awaitItem().content
                cancelAndIgnoreRemainingEvents()
            }

            // ----- Assert -----
            contents[2] shouldBe RandomPickWidgetUiModel.Content.Switching(eyebrow = "WHAT NEXT? · WANT TO READ")
            contents[3].shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()
        }

        @Test
        fun `a settings change while signed out shows no switching`() = runTest {
            // ----- Arrange -----
            val settingsChanges = MutableSharedFlow<RandomPickSettingsChange>(replay = 1)
            settingsChanges.tryEmit(changeTo(RandomPickSource.Owned))

            val flow = models(
                isSignedIn = false,
                resolution = resolved(RandomPickSource.Owned),
                settingsChanges = settingsChanges,
            )

            // ----- Act & Assert -----
            flow.test {
                awaitItem().content shouldBe RandomPickWidgetUiModel.Content.SignedOut
                cancelAndIgnoreRemainingEvents()
            }
        }

        private fun failingAfter(
            successes: Int,
            source: RandomPickSource,
        ): suspend (List<Book>, List<BookList>) -> Result<RandomPickResolution> {
            var calls = 0

            return { _, _ ->
                calls++

                if (calls <= successes) {
                    resolved(source)
                } else {
                    Result.failure(IllegalStateException("resolve failed"))
                }
            }
        }

        @Test
        fun `a failed resolve after switching restores the previous model`() = runTest {
            // ----- Arrange -----
            val settingsChanges = MutableSharedFlow<RandomPickSettingsChange>()

            val flow = randomPickWidgetModels(
                isSignedIn = flowOf(true),
                themeConfiguration = flowOf(ThemeConfiguration()),
                wantToRead = flowOf(emptyList()),
                lists = flowOf(emptyList()),
                changes = flowOf(Unit),
                settingsChanges = settingsChanges,
                resolve = failingAfter(
                    successes = 1,
                    source = RandomPickSource.WantToRead,
                ),
                coverUri = { _, _ -> null },
                coverHeightDp = flowOf(200),
                artwork = { null },
            )

            // ----- Act -----
            val contents = mutableListOf<RandomPickWidgetUiModel.Content>()
            flow.test {
                contents += awaitItem().content
                settingsChanges.emit(changeTo(RandomPickSource.Owned))
                contents += awaitItem().content
                contents += awaitItem().content
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            // ----- Assert -----
            contents[0].shouldBeInstanceOf<RandomPickWidgetUiModel.Content.Picked>()
            contents[1] shouldBe RandomPickWidgetUiModel.Content.Switching(eyebrow = "WHAT NEXT? · OWNED")
            contents[2] shouldBe contents[0]
        }

        @Test
        fun `a failed resolve after switching with no previous model shows loading`() = runTest {
            // ----- Arrange -----
            val settingsChanges = MutableSharedFlow<RandomPickSettingsChange>(replay = 1)
            settingsChanges.tryEmit(changeTo(RandomPickSource.Owned))

            val flow = models(
                resolution = Result.failure(IllegalStateException("resolve failed")),
                settingsChanges = settingsChanges,
            )

            // ----- Act -----
            val contents = mutableListOf<RandomPickWidgetUiModel.Content>()
            flow.test {
                contents += awaitItem().content
                contents += awaitItem().content
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            // ----- Assert -----
            contents shouldContainExactly listOf(
                RandomPickWidgetUiModel.Content.Switching(eyebrow = "WHAT NEXT? · OWNED"),
                RandomPickWidgetUiModel.Content.Loading,
            )
        }
    }
}
