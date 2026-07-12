package nl.rhaydus.softcover.feature.explore.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class EnrichDismissedContinueSeriesMetadataUseCaseTest {
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: EnrichDismissedContinueSeriesMetadataUseCase

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk(relaxed = true)
        booksRepository = mockk(relaxed = true)

        every {
            exploreRepository.dismissedContinueSeriesBooks
        } returns flowOf(emptyList())
        every {
            exploreRepository.dismissedContinueSeries
        } returns flowOf(emptyList())

        useCase = EnrichDismissedContinueSeriesMetadataUseCase(
            exploreRepository = exploreRepository,
            booksRepository = booksRepository,
        )
    }

    private fun dismissedBook(
        bookId: Int,
        title: String? = null,
    ): DismissedSeriesBook = DismissedSeriesBook(
        bookId = bookId,
        title = title,
        coverUrl = null,
        authorText = null,
        seriesName = null,
    )

    private fun dismissedSeries(
        seriesId: Int,
        seriesName: String? = null,
    ): DismissedSeries = DismissedSeries(
        seriesId = seriesId,
        seriesName = seriesName,
        coverUrl = null,
    )

    private fun bookInSeries(
        id: Int,
        seriesId: Int,
        seriesName: String = "Test Series",
    ): Book = PreviewData.baseBook.copy(
        id = id,
        title = "Book $id",
        coverUrl = "https://example.com/$id.jpg",
        bookSeries = BookSeries(
            id = seriesId,
            name = seriesName,
            amountOfBooks = 10,
        ),
    )

    @Nested
    inner class Idempotency {
        @Test
        fun `does nothing when no book or series rows need enrichment`() = runTest {
            // ----- Arrange -----
            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(listOf(dismissedBook(
                bookId = 1,
                title = "Already enriched",
            ),),)
            every {
                exploreRepository.dismissedContinueSeries
            } returns flowOf(listOf(dismissedSeries(
                seriesId = 10,
                seriesName = "Already enriched",
            ),),)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify(exactly = 0) { booksRepository.fetchBookById(any()) }
            verify(exactly = 0) { booksRepository.books }
            coVerify(exactly = 0) { exploreRepository.fetchNextInSeries(
                any(),
                any(),
            ) }
            coVerify(exactly = 0) { exploreRepository.dismissContinueSeriesBook(
                any(),
                any(),
                any(),
                any(),
                any(),
            ) }
            coVerify(exactly = 0) { exploreRepository.dismissContinueSeries(
                any(),
                any(),
                any(),
            ) }
        }

        @Test
        fun `only rows with null metadata are resolved, already-populated rows are skipped`() = runTest {
            // ----- Arrange -----
            val needsEnrichment = dismissedBook(
                bookId = 1,
                title = null,
            )
            val alreadyEnriched = dismissedBook(
                bookId = 2,
                title = "Foundation",
            )

            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(listOf(needsEnrichment, alreadyEnriched))

            val fetched = bookInSeries(
                id = 1,
                seriesId = 100,
            )

            coEvery {
                booksRepository.fetchBookById(id = 1)
            } returns fetched

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify(exactly = 1) { booksRepository.fetchBookById(id = 1) }
            coVerify(exactly = 0) { booksRepository.fetchBookById(id = 2) }
        }

        @Test
        fun `booksRepository books is not queried when no series rows need enrichment`() = runTest {
            // ----- Arrange -----
            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(listOf(dismissedBook(
                bookId = 1,
                title = null,
            ),),)

            coEvery {
                booksRepository.fetchBookById(id = 1)
            } returns bookInSeries(
                id = 1,
                seriesId = 100,
            )

            // ----- Act -----
            useCase()

            // ----- Assert -----
            verify(exactly = 0) { booksRepository.books }
        }
    }

    @Nested
    inner class BookResolution {
        @Test
        fun `resolves a book row via booksRepository fetchBookById and writes metadata back`() = runTest {
            // ----- Arrange -----
            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(listOf(dismissedBook(
                bookId = 42,
                title = null,
            ),),)

            val fetched = PreviewData.baseBook.copy(
                id = 42,
                title = "Dune",
                coverUrl = "https://example.com/dune.jpg",
                bookSeries = BookSeries(
                    id = 5,
                    name = "Dune Saga",
                    amountOfBooks = 6,
                ),
            )

            coEvery {
                booksRepository.fetchBookById(id = 42)
            } returns fetched

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                exploreRepository.dismissContinueSeriesBook(
                    bookId = 42,
                    title = "Dune",
                    coverUrl = "https://example.com/dune.jpg",
                    authorText = fetched.authorString,
                    seriesName = "Dune Saga",
                )
            }
        }
    }

    @Nested
    inner class SeriesResolution {
        @Test
        fun `resolves a series row from booksRepository books matching bookSeries id`() = runTest {
            // ----- Arrange -----
            every {
                exploreRepository.dismissedContinueSeries
            } returns flowOf(listOf(dismissedSeries(
                seriesId = 100,
                seriesName = null,
            ),),)

            val localBook = bookInSeries(
                id = 1,
                seriesId = 100,
                seriesName = "Foundation",
            )
            val unrelatedBook = bookInSeries(
                id = 2,
                seriesId = 999,
                seriesName = "Unrelated",
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(unrelatedBook, localBook))

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                exploreRepository.dismissContinueSeries(
                    seriesId = 100,
                    seriesName = "Foundation",
                    coverUrl = localBook.coverUrl,
                )
            }
            coVerify(exactly = 0) { exploreRepository.fetchNextInSeries(
                any(),
                any(),
            ) }
        }

        @Test
        fun `falls back to exploreRepository fetchNextInSeries when no local book matches the series`() = runTest {
            // ----- Arrange -----
            every {
                exploreRepository.dismissedContinueSeries
            } returns flowOf(listOf(dismissedSeries(
                seriesId = 200,
                seriesName = null,
            ),),)

            every {
                booksRepository.books
            } returns flowOf(emptyList())

            val seed = bookInSeries(
                id = 3,
                seriesId = 200,
                seriesName = "Foundation",
            )

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 200,
                    afterPosition = 0.0,
                )
            } returns seed

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                exploreRepository.dismissContinueSeries(
                    seriesId = 200,
                    seriesName = "Foundation",
                    coverUrl = seed.coverUrl,
                )
            }
        }

        @Test
        fun `leaves the series unenriched when the fetched seed has no bookSeries name`() = runTest {
            // ----- Arrange -----
            every {
                exploreRepository.dismissedContinueSeries
            } returns flowOf(listOf(dismissedSeries(
                seriesId = 300,
                seriesName = null,
            ),),)

            every {
                booksRepository.books
            } returns flowOf(emptyList())

            val seed = PreviewData.baseBook.copy(
                id = 4,
                title = "Untitled Series Seed",
                coverUrl = "https://example.com/seed.jpg",
                bookSeries = null,
            )

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 300,
                    afterPosition = 0.0,
                )
            } returns seed

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify(exactly = 0) {
                exploreRepository.dismissContinueSeries(
                    seriesId = 300,
                    seriesName = any(),
                    coverUrl = any(),
                )
            }
        }

        @Test
        fun `does not write back when neither a local book nor fetchNextInSeries resolves a seed`() = runTest {
            // ----- Arrange -----
            every {
                exploreRepository.dismissedContinueSeries
            } returns flowOf(listOf(dismissedSeries(
                seriesId = 400,
                seriesName = null,
            ),),)

            every {
                booksRepository.books
            } returns flowOf(emptyList())

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 400,
                    afterPosition = 0.0,
                )
            } returns null

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify(exactly = 0) { exploreRepository.dismissContinueSeries(
                any(),
                any(),
                any(),
            ) }
        }
    }

    @Nested
    inner class FailureIsolation {
        @Test
        fun `a throwing book resolve does not prevent other book rows from being enriched`() = runTest {
            // ----- Arrange -----
            val failingBook = dismissedBook(
                bookId = 1,
                title = null,
            )
            val succeedingBook = dismissedBook(
                bookId = 2,
                title = null,
            )

            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(listOf(failingBook, succeedingBook))

            coEvery {
                booksRepository.fetchBookById(id = 1)
            } throws RuntimeException("network error")

            val fetched = PreviewData.baseBook.copy(
                id = 2,
                title = "Neuromancer",
                coverUrl = "https://example.com/neuromancer.jpg",
                bookSeries = null,
            )

            coEvery {
                booksRepository.fetchBookById(id = 2)
            } returns fetched

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                exploreRepository.dismissContinueSeriesBook(
                    bookId = 2,
                    title = "Neuromancer",
                    coverUrl = "https://example.com/neuromancer.jpg",
                    authorText = fetched.authorString,
                    seriesName = null,
                )
            }
            coVerify(exactly = 0) {
                exploreRepository.dismissContinueSeriesBook(
                    bookId = 1,
                    title = any(),
                    coverUrl = any(),
                    authorText = any(),
                    seriesName = any(),
                )
            }
        }

        @Test
        fun `a throwing series resolve does not prevent other series rows from being enriched`() = runTest {
            // ----- Arrange -----
            val failingSeries = dismissedSeries(
                seriesId = 500,
                seriesName = null,
            )
            val succeedingSeries = dismissedSeries(
                seriesId = 600,
                seriesName = null,
            )

            every {
                exploreRepository.dismissedContinueSeries
            } returns flowOf(listOf(failingSeries, succeedingSeries))

            every {
                booksRepository.books
            } returns flowOf(emptyList())

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 500,
                    afterPosition = 0.0,
                )
            } throws RuntimeException("network error")

            val seed = bookInSeries(
                id = 7,
                seriesId = 600,
                seriesName = "Culture Series",
            )

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 600,
                    afterPosition = 0.0,
                )
            } returns seed

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                exploreRepository.dismissContinueSeries(
                    seriesId = 600,
                    seriesName = "Culture Series",
                    coverUrl = seed.coverUrl,
                )
            }
            coVerify(exactly = 0) { exploreRepository.dismissContinueSeries(
                seriesId = 500,
                any(),
                any(),
            ) }
        }

        @Test
        fun `invoke completes normally even when every row fails to resolve`() = runTest {
            // ----- Arrange -----
            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(listOf(dismissedBook(
                bookId = 1,
                title = null,
            ),),)
            every {
                exploreRepository.dismissedContinueSeries
            } returns flowOf(listOf(dismissedSeries(
                seriesId = 500,
                seriesName = null,
            ),),)

            coEvery {
                booksRepository.fetchBookById(id = 1)
            } throws RuntimeException("network error")
            every {
                booksRepository.books
            } returns flowOf(emptyList())
            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 500,
                    afterPosition = 0.0,
                )
            } throws RuntimeException("network error")

            // ----- Act & Assert (no exception propagates) -----
            useCase()
        }
    }
}
