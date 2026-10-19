package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.net.Uri
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.widget.presentation.model.BookArtworkUiModel
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressUris
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrentlyReadingWidgetModelsTest {
    private data class RenderCall(
        val fraction: Float,
        val widthDp: Int,
    )

    private val renders = mutableListOf<RenderCall>()
    private val coverRequests = mutableListOf<Int>()
    private val coverHeights = mutableListOf<Int>()
    private val artworkRequests = mutableListOf<Int>()
    private val coverUri = mockk<Uri>()
    private val progressUris = WavyProgressUris(
        track = mockk<Uri>(),
        indicator = mockk<Uri>(),
    )
    private val artwork = BookArtworkUiModel(
        backdrop = null,
        veil = mockk<Uri>(),
        veilLift = mockk<Uri>(),
        washPrimary = mockk<Uri>(),
        washTertiary = mockk<Uri>(),
    )
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
                displayTitle
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
        coverHeightDp: Flow<Int> = flowOf(200),
        barWidthsDp: Flow<Set<Int>> = flowOf(setOf(100)),
        coverUri: suspend (Book, Int) -> Uri? = { book, heightDp ->
            coverRequests += book.id
            coverHeights += heightDp
            this.coverUri
        },
    ): Flow<CurrentlyReadingWidgetUiModel> = currentlyReadingWidgetModels(
        isSignedIn = flowOf(isSignedIn),
        themeConfiguration = flowOf(ThemeConfiguration()),
        shelf = shelf,
        selectedBookId = selectedBookId,
        coverUri = coverUri,
        coverHeightDp = coverHeightDp,
        barWidthsDp = barWidthsDp,
        progressUris = { fraction, widthDp ->
            renders += RenderCall(
                fraction = fraction,
                widthDp = widthDp,
            )
            progressUris
        },
        artworkUris = { book ->
            artworkRequests += book.id
            artwork
        },
    )

    private fun CurrentlyReadingWidgetUiModel.reading() =
        content.shouldBeInstanceOf<CurrentlyReadingWidgetUiModel.Content.Reading>()

    @Nested
    inner class Content {
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
        fun `a single book shows an eyebrow without a position and cannot cycle`() = runTest {
            // ----- Arrange -----
            val flow = models(shelf = flowOf(listOf(book(1))))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val reading = awaitItem().reading()
                reading.eyebrow shouldBe "READING"
                reading.canCycle shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `several books show the position of the first book by default and can cycle`() = runTest {
            // ----- Arrange -----
            val flow = models(shelf = flowOf(books))

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val reading = awaitItem().reading()
                reading.title shouldBe "Title 1"
                reading.author shouldBe "Author 1"
                reading.eyebrow shouldBe "READING · 1/3"
                reading.canCycle shouldBe true
                cancelAndIgnoreRemainingEvents()
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
                val reading = awaitItem().reading()
                reading.title shouldBe "Title 3"
                reading.eyebrow shouldBe "READING · 3/3"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `a selected book that left the shelf falls back to the first book`() = runTest {
            // ----- Arrange -----
            val flow = models(
                shelf = flowOf(books),
                selectedBookId = flowOf(99),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                awaitItem().reading().title shouldBe "Title 1"
                cancelAndIgnoreRemainingEvents()
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
                awaitItem()

                // ----- Act -----
                shelf.value = books.reversed()

                // ----- Assert -----
                val reading = awaitItem().reading()
                reading.title shouldBe "Title 2"
                reading.eyebrow shouldBe "READING · 2/3"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class Images {
        @Test
        fun `the first emission has text with the images pending`() = runTest {
            // ----- Arrange -----
            val flow = models(
                shelf = flowOf(
                    listOf(
                        book(
                            id = 1,
                            percent = 40f,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            flow.test {
                // ----- Assert -----
                val reading = awaitItem().reading()
                reading.title shouldBe "Title 1"
                reading.progressLabel shouldBe "40%"
                reading.cover.shouldBeNull()
                reading.progress shouldBe emptyMap()
                reading.artwork.shouldBeNull()
                reading.imagesPending shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `the stage after a book without a cover or artwork no longer has its images pending`() = runTest {
            // ----- Arrange -----
            val flow = models(
                shelf = flowOf(listOf(book(1))),
                coverUri = { _, _ -> null },
            )

            // ----- Act -----
            flow.test {
                val text = awaitItem().reading()
                val images = awaitItem().reading()
                cancelAndIgnoreRemainingEvents()

                // ----- Assert -----
                text.imagesPending shouldBe true
                images.cover.shouldBeNull()
                images.imagesPending shouldBe false
            }
        }

        @Test
        fun `the second emission carries the stored image uris for the shown book`() = runTest {
            // ----- Arrange -----
            val flow = models(
                shelf = flowOf(books),
                selectedBookId = flowOf(2),
                barWidthsDp = flowOf(setOf(90, 140)),
            )

            // ----- Act -----
            flow.test {
                val text = awaitItem().reading()
                val images = awaitItem().reading()
                awaitComplete()

                // ----- Assert -----
                images.copy(
                    cover = null,
                    progress = emptyMap(),
                    artwork = null,
                    imagesPending = true,
                ) shouldBe text
                images.cover shouldBe coverUri
                images.progress shouldBe mapOf(
                    90 to progressUris,
                    140 to progressUris,
                )
                images.artwork shouldBe artwork
            }
        }

        @Test
        fun `images are requested for the shown book only`() = runTest {
            // ----- Arrange -----
            val flow = models(
                shelf = flowOf(books),
                selectedBookId = flowOf(2),
            )

            // ----- Act -----
            flow.test {
                awaitItem()
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            coverRequests shouldContainExactly listOf(2)
        }

        @Test
        fun `progress is rendered from the shown book percentage once per requested width`() = runTest {
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
                barWidthsDp = flowOf(setOf(90, 140, 200)),
            )

            // ----- Act -----
            flow.test {
                awaitItem()
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            renders shouldContainExactly listOf(90, 140, 200).map {
                RenderCall(
                    fraction = 0.5f,
                    widthDp = it,
                )
            }
        }

        @Test
        fun `a width whose bar could not be stored is left out of the progress map`() = runTest {
            // ----- Arrange -----
            val flow = currentlyReadingWidgetModels(
                isSignedIn = flowOf(true),
                themeConfiguration = flowOf(ThemeConfiguration()),
                shelf = flowOf(listOf(book(1))),
                selectedBookId = flowOf(null),
                coverUri = { _, _ -> null },
                coverHeightDp = flowOf(200),
                barWidthsDp = flowOf(setOf(90, 140)),
                progressUris = { _, widthDp -> if (widthDp == 90) null else progressUris },
            )

            // ----- Act -----
            flow.test {
                awaitItem()
                val images = awaitItem().reading()
                awaitComplete()

                // ----- Assert -----
                images.cover.shouldBeNull()
                images.progress shouldBe mapOf(140 to progressUris)
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
                awaitItem()

                // ----- Act -----
                shelf.value = listOf(
                    book(
                        id = 1,
                        percent = 20f,
                    ),
                )
                awaitItem()
                awaitItem()

                // ----- Assert -----
                renders.map { it.fraction } shouldContainExactly listOf(0.1f, 0.2f)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `selecting another book while images are pending drops the stale images`() = runTest {
            // ----- Arrange -----
            val gate = CompletableDeferred<Uri?>()
            val selected = MutableStateFlow<Int?>(1)
            val flow = models(
                shelf = flowOf(books),
                selectedBookId = selected,
                coverUri = { book, _ -> if (book.id == 1) gate.await() else coverUri },
            )

            flow.test {
                awaitItem().reading().title shouldBe "Title 1"

                // ----- Act -----
                selected.value = 2
                gate.complete(coverUri)

                // ----- Assert -----
                awaitItem().reading().title shouldBe "Title 2"
                val images = awaitItem().reading()
                images.title shouldBe "Title 2"
                images.cover shouldBe coverUri
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `a new set of widths re-emits the model with progress for exactly those widths`() = runTest {
            // ----- Arrange -----
            val widths = MutableStateFlow(setOf(90))
            val flow = models(
                shelf = flowOf(books),
                barWidthsDp = widths,
            )

            flow.test {
                awaitItem()
                awaitItem().reading().progress.keys shouldBe setOf(90)

                // ----- Act -----
                widths.value = setOf(140, 200)

                // ----- Assert -----
                awaitItem().reading().progress.keys shouldBe setOf(140, 200)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `the cover and artwork are produced once per shown book however often the widths change`() = runTest {
            // ----- Arrange -----
            val widths = MutableStateFlow(setOf(90))
            val flow = models(
                shelf = flowOf(books),
                selectedBookId = flowOf(2),
                barWidthsDp = widths,
            )

            flow.test {
                awaitItem()
                awaitItem()

                // ----- Act -----
                widths.value = setOf(140)
                awaitItem()
                widths.value = setOf(200)
                awaitItem()

                // ----- Assert -----
                coverRequests shouldContainExactly listOf(2)
                artworkRequests shouldContainExactly listOf(2)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `a new cover height produces the cover again at that height`() = runTest {
            // ----- Arrange -----
            val heights = MutableStateFlow(200)
            val flow = models(
                shelf = flowOf(books),
                coverHeightDp = heights,
            )

            flow.test {
                awaitItem()
                awaitItem()

                // ----- Act -----
                heights.value = 320
                awaitItem()

                // ----- Assert -----
                coverHeights shouldContainExactly listOf(200, 320)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
