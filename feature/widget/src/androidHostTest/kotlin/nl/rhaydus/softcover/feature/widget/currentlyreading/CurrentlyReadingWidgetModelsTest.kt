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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.widget.progress.WavyProgressBitmaps
import org.junit.jupiter.api.Test

class CurrentlyReadingWidgetModelsTest {
    private val renderedFractions = mutableListOf<Float>()
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
                userBookRead
            } returns read
        }
    }

    private fun models(
        isSignedIn: Boolean = true,
        shelf: Flow<List<Book>>,
        selectedBookId: Flow<Int?> = flowOf(null),
    ): Flow<CurrentlyReadingWidgetUiModel> = currentlyReadingWidgetModels(
        isSignedIn = flowOf(isSignedIn),
        themeConfiguration = flowOf(ThemeConfiguration()),
        shelf = shelf,
        selectedBookId = selectedBookId,
        loadCover = { book ->
            loadedBookIds += book.id
            null as Bitmap?
        },
        renderProgress = { fraction ->
            renderedFractions += fraction
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
            renderedFractions shouldContainExactly listOf(0.4f)
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
}
