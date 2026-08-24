package nl.rhaydus.softcover.feature.explore.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.model.SeriesContinuationSeed
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetContinueSeriesBooksUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: GetContinueSeriesBooksUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        exploreRepository = mockk()

        every {
            exploreRepository.dismissedContinueSeriesBooks
        } returns flowOf(emptyList())
        every {
            exploreRepository.dismissedContinueSeriesIds
        } returns flowOf(emptyList())
        coEvery {
            exploreRepository.fetchNextBooksInSeries(seeds = any())
        } returns emptyList()

        useCase = GetContinueSeriesBooksUseCase(
            booksRepository = booksRepository,
            exploreRepository = exploreRepository,
        )
    }

    private fun stubUserBook(
        status: BookStatus,
        lastReadDate: String? = null,
    ): UserBook = mockk {
        every {
            this@mockk.status
        } returns status

        every {
            this@mockk.lastReadDate
        } returns lastReadDate
    }

    private fun bookInSeries(
        id: Int,
        seriesId: Int,
        seriesName: String = "Test Series",
        amountOfBooks: Int = 10,
        positionsInSeries: List<Double> = emptyList(),
        status: BookStatus,
        lastReadDate: String? = null,
    ): Book = PreviewData.baseBook.copy(
        id = id,
        bookSeries = BookSeries(
            id = seriesId,
            name = seriesName,
            amountOfBooks = amountOfBooks,
        ),
        positionsInSeries = positionsInSeries,
        userBook = stubUserBook(
            status,
            lastReadDate,
        ),
        userBookRead = null,
    )

    private fun dismissedBook(
        bookId: Int,
        seriesId: Int? = null,
        seriesPosition: Double? = null,
    ): DismissedSeriesBook = DismissedSeriesBook(
        bookId = bookId,
        title = null,
        coverUrl = null,
        authorText = null,
        seriesName = null,
        seriesId = seriesId,
        seriesPosition = seriesPosition,
    )

    // ----- Invoke -----

    @Nested
    inner class Invoke {
        @Test
        fun `returns empty list when books flow emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(emptyList())

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `excludes series entirely when any book in the series has DidNotFinish status`() = runTest {
            // ----- Arrange -----
            val dnfBook = bookInSeries(
                id = 1,
                seriesId = 100,
                positionsInSeries = listOf(1.0),
                status = BookStatus.DidNotFinish,
            )
            val readBook = bookInSeries(
                id = 2,
                seriesId = 100,
                positionsInSeries = listOf(2.0),
                status = BookStatus.Read,
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(dnfBook, readBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `seeds continuation from the highest positionsInSeries among Reading and Read books`() = runTest {
            // ----- Arrange -----
            val lowerBook = bookInSeries(
                id = 1,
                seriesId = 200,
                amountOfBooks = 10,
                positionsInSeries = listOf(2.0),
                status = BookStatus.Read,
            )
            val higherBook = bookInSeries(
                id = 2,
                seriesId = 200,
                amountOfBooks = 10,
                positionsInSeries = listOf(4.0),
                status = BookStatus.Read,
            )
            val nextBook: Book = mockk(relaxed = true)

            every {
                booksRepository.books
            } returns flowOf(listOf(lowerBook, higherBook))

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
            seedsSlot.captured shouldBe listOf(SeriesContinuationSeed(
                seriesId = 200,
                afterPosition = 4.0,
            ),)
        }

        @Test
        fun `skips series where the user max position is at or beyond amountOfBooks`() = runTest {
            // ----- Arrange -----
            val finalBook = bookInSeries(
                id = 1,
                seriesId = 300,
                amountOfBooks = 3,
                positionsInSeries = listOf(3.0),
                status = BookStatus.Read,
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(finalBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            }
        }

        @Test
        fun `WantToRead books do not seed continuation`() = runTest {
            // ----- Arrange -----
            val wantToReadBook = bookInSeries(
                id = 1,
                seriesId = 400,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.WantToRead,
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(wantToReadBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            }
        }

        @Test
        fun `None-status books do not seed continuation`() = runTest {
            // ----- Arrange -----
            val noneBook = bookInSeries(
                id = 1,
                seriesId = 500,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.None,
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(noneBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            }
        }

        @Test
        fun `books missing bookSeries are ignored`() = runTest {
            // ----- Arrange -----
            val noSeriesBook = PreviewData.baseBook.copy(
                id = 1,
                bookSeries = null,
                positionsInSeries = listOf(1.0),
                userBook = stubUserBook(BookStatus.Read),
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(noSeriesBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            }
        }

        @Test
        fun `books missing positionsInSeries are ignored`() = runTest {
            // ----- Arrange -----
            val noPositionBook = PreviewData.baseBook.copy(
                id = 1,
                bookSeries = BookSeries(
                    id = 600,
                    name = "Series",
                    amountOfBooks = 5,
                ),
                positionsInSeries = emptyList(),
                userBook = stubUserBook(BookStatus.Read),
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(noPositionBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            }
        }

        @Test
        fun `float position 1-0 fetches with afterPosition 1-0`() = runTest {
            // ----- Arrange -----
            val book = bookInSeries(
                id = 1,
                seriesId = 700,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Reading,
            )
            val nextBook: Book = mockk(relaxed = true)

            every {
                booksRepository.books
            } returns flowOf(listOf(book))

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
            seedsSlot.captured shouldBe listOf(SeriesContinuationSeed(
                seriesId = 700,
                afterPosition = 1.0,
            ),)
        }

        @Test
        fun `float position 1-5 fetches with afterPosition 1-5`() = runTest {
            // ----- Arrange -----
            val book = bookInSeries(
                id = 1,
                seriesId = 800,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.5),
                status = BookStatus.Reading,
            )
            val nextBook: Book = mockk(relaxed = true)

            every {
                booksRepository.books
            } returns flowOf(listOf(book))

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
            seedsSlot.captured shouldBe listOf(SeriesContinuationSeed(
                seriesId = 800,
                afterPosition = 1.5,
            ),)
        }

        @Test
        fun `batched fetch resolves both qualifying series in a single call`() = runTest {
            // ----- Arrange -----
            val bookA = bookInSeries(
                id = 1,
                seriesId = 901,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )
            val bookB = bookInSeries(
                id = 2,
                seriesId = 902,
                amountOfBooks = 5,
                positionsInSeries = listOf(2.0),
                status = BookStatus.Reading,
            )
            val nextA: Book = mockk(relaxed = true)
            val nextB: Book = mockk(relaxed = true)

            every {
                booksRepository.books
            } returns flowOf(listOf(bookA, bookB))

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextA, nextB)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result.toSet() shouldBe setOf(nextA, nextB)
            seedsSlot.captured.toSet() shouldBe setOf(
                SeriesContinuationSeed(
                    901,
                    1.0,
                ),
                SeriesContinuationSeed(
                    902,
                    2.0,
                ),
            )
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = any())
            }
        }

        @Test
        fun `series with null lastReadDate still fetches, ordered after series with a real date`() = runTest {
            // ----- Arrange -----
            val nullDateBook = bookInSeries(
                id = 1,
                seriesId = 950,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
                lastReadDate = null,
            )
            val datedBooks = (1..8).map { index ->
                bookInSeries(
                    id = index + 1,
                    seriesId = 900 + index,
                    amountOfBooks = 5,
                    positionsInSeries = listOf(1.0),
                    status = BookStatus.Read,
                    lastReadDate = "2024-01-0$index",
                )
            }

            every {
                booksRepository.books
            } returns flowOf(datedBooks + nullDateBook)

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns emptyList()

            // ----- Act -----
            useCase().first()

            // ----- Assert -----
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = any())
            }
            seedsSlot.captured.last() shouldBe SeriesContinuationSeed(
                seriesId = 950,
                afterPosition = 1.0,
            )
            seedsSlot.captured.size shouldBe 9
        }

        @Test
        fun `single batched call resolves every qualifying series, however many there are`() = runTest {
            // ----- Arrange -----
            val books = (1..12).map { index ->
                bookInSeries(
                    id = index,
                    seriesId = 900 + index,
                    amountOfBooks = 5,
                    positionsInSeries = listOf(1.0),
                    status = BookStatus.Read,
                    lastReadDate = "2024-01-0$index",
                )
            }

            every {
                booksRepository.books
            } returns flowOf(books)

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns (1..12).map { mockk<Book>(relaxed = true) }

            // ----- Act -----
            useCase().first()

            // ----- Assert -----
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = any())
            }
            seedsSlot.captured.map { it.seriesId }.toSet() shouldBe (901..912).toSet()
        }

        @Test
        fun `a thrown exception from the batch fetch empties the whole shelf rather than propagating`() = runTest {
            // ----- Arrange -----
            val bookA = bookInSeries(
                id = 1,
                seriesId = 1001,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(bookA))

            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = any())
            } throws RuntimeException("network error")

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `a series the batch returns no book for simply does not appear in the result`() = runTest {
            // ----- Arrange -----
            // afterPosition must stay below amountOfBooks (5) - at or above it, deriveSeeds'
            // cursor >= amountOfBooks check excludes the series before any fetch is attempted,
            // which would make the setOf(1101, 1102) assertion below vacuous.
            val bookA = bookInSeries(
                id = 1,
                seriesId = 1101,
                amountOfBooks = 5,
                positionsInSeries = listOf(4.0),
                status = BookStatus.Read,
            )
            val bookB = bookInSeries(
                id = 2,
                seriesId = 1102,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )
            val nextB: Book = mockk(relaxed = true)

            every {
                booksRepository.books
            } returns flowOf(listOf(bookA, bookB))

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextB)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextB)
            seedsSlot.captured.map { it.seriesId }.toSet() shouldBe setOf(1101, 1102)
        }

        @Test
        fun `compilation marked Read seeds continuation after its highest position`() = runTest {
            // ----- Arrange -----
            val compilation = bookInSeries(
                id = 1,
                seriesId = 1200,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0, 2.0, 3.0),
                status = BookStatus.Read,
            )
            val nextBook: Book = mockk(relaxed = true)

            every {
                booksRepository.books
            } returns flowOf(listOf(compilation))

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
            seedsSlot.captured shouldBe listOf(SeriesContinuationSeed(
                seriesId = 1200,
                afterPosition = 3.0,
            ),)
        }

        @Test
        fun `compilation marked Reading also seeds from its highest position`() = runTest {
            // ----- Arrange -----
            val compilation = bookInSeries(
                id = 1,
                seriesId = 1300,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0, 2.0, 3.0),
                status = BookStatus.Reading,
            )
            val nextBook: Book = mockk(relaxed = true)

            every {
                booksRepository.books
            } returns flowOf(listOf(compilation))

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
            seedsSlot.captured shouldBe listOf(SeriesContinuationSeed(
                seriesId = 1300,
                afterPosition = 3.0,
            ),)
        }

        @Test
        fun `mix of compilation Read and singleton Read picks the higher max`() = runTest {
            // ----- Arrange -----
            val compilation = bookInSeries(
                id = 1,
                seriesId = 1400,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0, 2.0),
                status = BookStatus.Read,
            )
            val singleton = bookInSeries(
                id = 2,
                seriesId = 1400,
                amountOfBooks = 5,
                positionsInSeries = listOf(3.0),
                status = BookStatus.Read,
            )
            val nextBook: Book = mockk(relaxed = true)

            every {
                booksRepository.books
            } returns flowOf(listOf(compilation, singleton))

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
            seedsSlot.captured shouldBe listOf(SeriesContinuationSeed(
                seriesId = 1400,
                afterPosition = 3.0,
            ),)
        }

        @Test
        fun `compilation covering full series is excluded`() = runTest {
            // ----- Arrange -----
            val compilation = bookInSeries(
                id = 1,
                seriesId = 1500,
                amountOfBooks = 3,
                positionsInSeries = listOf(1.0, 2.0, 3.0),
                status = BookStatus.Read,
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(compilation))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            }
        }

        @Test
        fun `dismissed series id excludes it from the batch request`() = runTest {
            // ----- Arrange -----
            val book = bookInSeries(
                id = 1,
                seriesId = 1600,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(book))
            every {
                exploreRepository.dismissedContinueSeriesIds
            } returns flowOf(listOf(1600))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            }
        }

        @Test
        fun `dismissed book id filters out matching fetched book from the final emission`() = runTest {
            // ----- Arrange -----
            val book = bookInSeries(
                id = 1,
                seriesId = 1700,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )
            val nextBook: Book = mockk {
                every {
                    id
                } returns 999
            }

            every {
                booksRepository.books
            } returns flowOf(listOf(book))
            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(listOf(dismissedBook(bookId = 999)))

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            seedsSlot.captured shouldBe listOf(SeriesContinuationSeed(
                seriesId = 1700,
                afterPosition = 1.0,
            ),)
        }

        @Test
        fun `result list updates reactively when dismissed series ids change between emissions`() = runTest {
            // ----- Arrange -----
            val book = bookInSeries(
                id = 1,
                seriesId = 1800,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )
            val nextBook: Book = mockk(relaxed = true)

            val dismissedSeriesIds = MutableStateFlow(emptyList<Int>())

            every {
                booksRepository.books
            } returns flowOf(listOf(book))
            every {
                exploreRepository.dismissedContinueSeriesIds
            } returns dismissedSeriesIds

            coEvery {
                exploreRepository.fetchNextBooksInSeries(
                    seeds = listOf(SeriesContinuationSeed(
                        seriesId = 1800,
                        afterPosition = 1.0,
                    ),),
                )
            } returns listOf(nextBook)
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            } returns emptyList()

            // ----- Act & Assert -----
            useCase().test {
                // First emission: series not dismissed, book should appear
                awaitItem() shouldBe listOf(nextBook)

                // Dismiss the series
                dismissedSeriesIds.value = listOf(1800)

                // Second emission: series dismissed, result should be empty
                awaitItem() shouldBe emptyList()
            }
        }
    }

    // ----- DismissalCursorAdvance -----

    @Nested
    inner class DismissalCursorAdvance {
        @Test
        fun `dismissing the suggested book advances the cursor past it instead of refetching the same position`() = runTest {
            // ----- Arrange -----
            val readBook = bookInSeries(
                id = 2001,
                seriesId = 2000,
                amountOfBooks = 5,
                positionsInSeries = listOf(2.0),
                status = BookStatus.Read,
            )
            val nextBook: Book = mockk {
                every {
                    id
                } returns 2004
            }

            every {
                booksRepository.books
            } returns flowOf(listOf(readBook))
            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(
                listOf(
                    dismissedBook(
                        bookId = 2003,
                        seriesId = 2000,
                        seriesPosition = 3.0,
                    ),
                ),
            )

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
            seedsSlot.captured shouldBe listOf(SeriesContinuationSeed(
                seriesId = 2000,
                afterPosition = 3.0,
            ),)
        }

        @Test
        fun `dismissed position recorded under a different series does not move this series' cursor`() = runTest {
            // ----- Arrange -----
            val readBook = bookInSeries(
                id = 2101,
                seriesId = 2100,
                amountOfBooks = 5,
                positionsInSeries = listOf(2.0),
                status = BookStatus.Read,
            )
            val nextBook: Book = mockk(relaxed = true)

            every {
                booksRepository.books
            } returns flowOf(listOf(readBook))
            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(
                listOf(
                    dismissedBook(
                        bookId = 9998,
                        seriesId = 9999,
                        seriesPosition = 100.0,
                    ),
                ),
            )

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
            seedsSlot.captured shouldBe listOf(SeriesContinuationSeed(
                seriesId = 2100,
                afterPosition = 2.0,
            ),)
        }

        @Test
        fun `legacy dismissed row with no series metadata does not corrupt the cursor and is still excluded by the id filter`() = runTest {
            // ----- Arrange -----
            val readBook = bookInSeries(
                id = 2201,
                seriesId = 2200,
                amountOfBooks = 5,
                positionsInSeries = listOf(2.0),
                status = BookStatus.Read,
            )
            val nextBook: Book = mockk {
                every {
                    id
                } returns 2299
            }

            every {
                booksRepository.books
            } returns flowOf(listOf(readBook))
            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(
                listOf(
                    dismissedBook(
                        bookId = 2299,
                        seriesId = null,
                        seriesPosition = null,
                    ),
                ),
            )

            val seedsSlot = slot<List<SeriesContinuationSeed>>()
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            seedsSlot.captured shouldBe listOf(SeriesContinuationSeed(
                seriesId = 2200,
                afterPosition = 2.0,
            ),)
        }

        @Test
        fun `dismissed position at or beyond amountOfBooks drops the series entirely`() = runTest {
            // ----- Arrange -----
            val readBook = bookInSeries(
                id = 2301,
                seriesId = 2300,
                amountOfBooks = 3,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(readBook))
            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(
                listOf(
                    dismissedBook(
                        bookId = 2302,
                        seriesId = 2300,
                        seriesPosition = 3.0,
                    ),
                ),
            )

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 1) {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            }
        }
    }
}
