package nl.rhaydus.softcover.feature.widget.currentlyreading

import android.graphics.Bitmap
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.widget.progress.WavyProgressBitmaps
import org.junit.jupiter.api.Test

class CurrentlyReadingWidgetModelsTest {
    private data class RenderCall(
        val fraction: Float,
        val widthDp: Int,
    )

    private val renders = mutableListOf<RenderCall>()
    private val loadedBookIds = mutableListOf<Int>()
    private val progressBitmaps = mockk<WavyProgressBitmaps>()
    private val books = listOf(
        book(1),
        book(2),
        book(3),
    )

    private fun book(
        id: Int,
        percent: Float = 0f,
    ): Book {
        val read = mockk<UserBookRead> {
            every {
                progress
            } returns percent
        }

        return mockk<Book> {
            every {
                this@mockk.id
            } returns id
            every {
                title
            } returns "Title $id"
            every {
                authorString
            } returns "Author $id"
            every {
                currentRead
            } returns read
        }
    }

    private fun models(
        isSignedIn: Boolean = true,
        shelf: Flow<List<Book>>,
        selectedBookId: Flow<Int?> = flowOf(null),
        barWidthsDp: List<Int> = listOf(100),
    ): Flow<CurrentlyReadingWidgetUiModel> = currentlyReadingWidgetModels(
        isSignedIn = flowOf(isSignedIn),
        themeConfiguration = flowOf(ThemeConfiguration()),
        shelf = shelf,
        selectedBookId = selectedBookId,
        loadCover = { book ->
            loadedBookIds += book.id
            null as Bitmap?
        },
        barWidthsDp = barWidthsDp,
        renderProgress = { fraction, widthDp ->
            renders += RenderCall(
                fraction = fraction,
                widthDp = widthDp,
            )
            progressBitmaps
        },
    )

    @Test
    fun `signed out shows the signed out content regardless of the shelf`() = runTest {
        // ----- Arrange -----
        val flow = models(
            isSignedIn = false,
            shelf = flowOf(listOf(book(1))),
        )

        // ----- Act -----
        flow.test {
            // ----- Assert -----
            awaitItem().content shouldBe CurrentlyReadingWidgetUiModel.Content.SignedOut
            awaitComplete()
        }
    }

    @Test
    fun `signed in with an empty shelf shows the empty content`() = runTest {
        // ----- Arrange -----
        val flow = models(shelf = flowOf(emptyList()))

        // ----- Act -----
        flow.test {
            // ----- Assert -----
            awaitItem().content shouldBe CurrentlyReadingWidgetUiModel.Content.Empty
            awaitComplete()
        }
    }

    @Test
    fun `a single book shows no position indicator`() = runTest {
        // ----- Arrange -----
        val flow = models(shelf = flowOf(listOf(book(1))))

        // ----- Act -----
        flow.test {
            // ----- Assert -----
            val reading = awaitItem().content.shouldBeInstanceOf<CurrentlyReadingWidgetUiModel.Content.Reading>()
            reading.position.shouldBeNull()
            awaitComplete()
        }
    }

    @Test
    fun `several books show the position of the first book by default`() = runTest {
        // ----- Arrange -----
        val flow = models(shelf = flowOf(books))

        // ----- Act -----
        flow.test {
            // ----- Assert -----
            val reading = awaitItem().content.shouldBeInstanceOf<CurrentlyReadingWidgetUiModel.Content.Reading>()
            reading.title shouldBe "Title 1"
            reading.position shouldBe "1/3"
            awaitComplete()
        }
    }

    @Test
    fun `the shown book follows the selected book id`() = runTest {
        // ----- Arrange -----
        val flow = models(
            shelf = flowOf(books),
            selectedBookId = flowOf(3),
        )

        // ----- Act -----
        flow.test {
            // ----- Assert -----
            val reading = awaitItem().content.shouldBeInstanceOf<CurrentlyReadingWidgetUiModel.Content.Reading>()
            reading.title shouldBe "Title 3"
            reading.position shouldBe "3/3"
            awaitComplete()
        }
    }

    @Test
    fun `the cover is loaded for the shown book only`() = runTest {
        // ----- Arrange -----
        val flow = models(
            shelf = flowOf(books),
            selectedBookId = flowOf(2),
        )

        // ----- Act -----
        flow.test {
            awaitItem()
            awaitComplete()
        }

        // ----- Assert -----
        loadedBookIds shouldContainExactly listOf(2)
    }

    @Test
    fun `progress is rendered from the shown book percentage as a fraction`() = runTest {
        // ----- Arrange -----
        val partlyRead = book(
            id = 1,
            percent = 40f,
        )
        val flow = models(shelf = flowOf(listOf(partlyRead)))

        // ----- Act -----
        flow.test {
            // ----- Assert -----
            val reading = awaitItem().content.shouldBeInstanceOf<CurrentlyReadingWidgetUiModel.Content.Reading>()
            reading.progressLabel shouldBe "40%"
            renders.map { it.fraction } shouldContainExactly listOf(0.4f)
            awaitComplete()
        }
    }

    @Test
    fun `only the shown book bar is rendered, once per requested width`() = runTest {
        // ----- Arrange -----
        val flow = models(
            shelf = flowOf(
                listOf(
                    book(
                        id = 1,
                        percent = 10f,
                    ),
                    book(
                        id = 2,
                        percent = 50f,
                    ),
                ),
            ),
            selectedBookId = flowOf(2),
            barWidthsDp = listOf(90, 140, 200),
        )

        // ----- Act -----
        flow.test {
            // ----- Assert -----
            val reading = awaitItem().content.shouldBeInstanceOf<CurrentlyReadingWidgetUiModel.Content.Reading>()
            reading.progress.keys.toList() shouldContainExactly listOf(90, 140, 200)
            renders shouldContainExactly listOf(
                RenderCall(
                    fraction = 0.5f,
                    widthDp = 90,
                ),
                RenderCall(
                    fraction = 0.5f,
                    widthDp = 140,
                ),
                RenderCall(
                    fraction = 0.5f,
                    widthDp = 200,
                ),
            )
            awaitComplete()
        }
    }

    @Test
    fun `reordering the shelf keeps the selected book shown`() = runTest {
        // ----- Arrange -----
        val shelf = MutableStateFlow(books)
        val flow = models(
            shelf = shelf,
            selectedBookId = flowOf(2),
        )

        flow.test {
            awaitItem()

            // ----- Act -----
            shelf.value = books.reversed()

            // ----- Assert -----
            val reading = awaitItem().content.shouldBeInstanceOf<CurrentlyReadingWidgetUiModel.Content.Reading>()
            reading.title shouldBe "Title 2"
            reading.position shouldBe "2/3"
        }
    }

    @Test
    fun `re-emitting the same shelf renders no new bars`() = runTest {
        // ----- Arrange -----
        val shelf = MutableSharedFlow<List<Book>>(replay = 1)
        val shelfBooks = listOf(
            book(
                id = 1,
                percent = 10f,
            ),
        )
        shelf.tryEmit(shelfBooks)
        val flow = models(shelf = shelf)

        flow.test {
            awaitItem()

            // ----- Act -----
            shelf.tryEmit(shelfBooks)
            awaitItem()

            // ----- Assert -----
            renders.map { it.fraction } shouldContainExactly listOf(0.1f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cycling to another book and back reuses the first book bars`() = runTest {
        // ----- Arrange -----
        val selected = MutableStateFlow<Int?>(1)
        val flow = models(
            shelf = flowOf(
                listOf(
                    book(
                        id = 1,
                        percent = 10f,
                    ),
                    book(
                        id = 2,
                        percent = 50f,
                    ),
                ),
            ),
            selectedBookId = selected,
        )

        flow.test {
            awaitItem()
            selected.value = 2
            awaitItem()

            // ----- Act -----
            selected.value = 1
            awaitItem()

            // ----- Assert -----
            renders.map { it.fraction } shouldContainExactly listOf(0.1f, 0.5f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a progress change for the shown book renders its bar again`() = runTest {
        // ----- Arrange -----
        val shelf = MutableStateFlow(
            listOf(
                book(
                    id = 1,
                    percent = 10f,
                ),
            ),
        )
        val flow = models(shelf = shelf)

        flow.test {
            awaitItem()

            // ----- Act -----
            shelf.value = listOf(
                book(
                    id = 1,
                    percent = 20f,
                ),
            )
            awaitItem()

            // ----- Assert -----
            renders.map { it.fraction } shouldContainExactly listOf(0.1f, 0.2f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a book that left the shelf is rendered again when it comes back`() = runTest {
        // ----- Arrange -----
        val first = book(
            id = 1,
            percent = 10f,
        )
        val second = book(
            id = 2,
            percent = 50f,
        )
        val shelf = MutableStateFlow(listOf(first, second))
        val selected = MutableStateFlow<Int?>(1)
        val flow = models(
            shelf = shelf,
            selectedBookId = selected,
        )

        flow.test {
            awaitItem()
            selected.value = 2
            awaitItem()
            shelf.value = listOf(second)
            awaitItem()
            shelf.value = listOf(first, second)
            awaitItem()

            // ----- Act -----
            selected.value = 1
            awaitItem()

            // ----- Assert -----
            renders.map { it.fraction } shouldContainExactly listOf(0.1f, 0.5f, 0.1f)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
