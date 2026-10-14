package nl.rhaydus.softcover.feature.explore.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.model.SeriesContinuationSeed
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
            seedsSlot.captured shouldBe listOf(
                SeriesContinuationSeed(
                    seriesId = 200,
                    afterPosition = 4.0,
                ),
            )
        }

        @Test
        fun `a series with a Paused book counts as started and seeds from its position`() = runTest {
            // ----- Arrange -----
            val pausedBook = bookInSeries(
                id = 1,
                seriesId = 250,
                amountOfBooks = 10,
                positionsInSeries = listOf(3.0),
                status = BookStatus.Paused,
            )
            val nextBook: Book = mockk(relaxed = true)

            every {
                booksRepository.books
            } returns flowOf(listOf(pausedBook))

            val seedsSlot = slot<List<SeriesContinuationSeed>>()

            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = capture(seedsSlot))
            } returns listOf(nextBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)

            val expectedSeed = SeriesContinuationSeed(
                seriesId = 250,
                afterPosition = 3.0,
            )
            seedsSlot.captured shouldBe listOf(expectedSeed)
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
            seedsSlot.captured shouldBe listOf(
                SeriesContinuationSeed(
                    seriesId = 1200,
                    afterPosition = 3.0,
                ),
            )
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
            seedsSlot.captured shouldBe listOf(
                SeriesContinuationSeed(
                    seriesId = 1400,
                    afterPosition = 3.0,
                ),
            )
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
            seedsSlot.captured shouldBe listOf(
                SeriesContinuationSeed(
                    seriesId = 1700,
                    afterPosition = 1.0,
                ),
            )
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
            val nextBook: Book = mockk {
                every {
                    id
                } returns 1801
                every {
                    bookSeries
                } returns BookSeries(
                    id = 1800,
                    name = "Test Series",
                    amountOfBooks = 5,
                )
            }

            val dismissedSeriesIds = MutableStateFlow(emptyList<Int>())

            every {
                booksRepository.books
            } returns flowOf(listOf(book))
            every {
                exploreRepository.dismissedContinueSeriesIds
            } returns dismissedSeriesIds

            coEvery {
                exploreRepository.fetchNextBooksInSeries(
                    seeds = listOf(
                        SeriesContinuationSeed(
                            seriesId = 1800,
                            afterPosition = 1.0,
                        ),
                    ),
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
                cancelAndIgnoreRemainingEvents()
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
                every {
                    bookSeries
                } returns null
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
            seedsSlot.captured shouldBe listOf(
                SeriesContinuationSeed(
                    seriesId = 2000,
                    afterPosition = 3.0,
                ),
            )
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
            seedsSlot.captured shouldBe listOf(
                SeriesContinuationSeed(
                    seriesId = 2100,
                    afterPosition = 2.0,
                ),
            )
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
            seedsSlot.captured shouldBe listOf(
                SeriesContinuationSeed(
                    seriesId = 2200,
                    afterPosition = 2.0,
                ),
            )
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

    // ----- ImmediateDismissal -----

    @Nested
    inner class ImmediateDismissal {
        private fun fetchedBook(
            id: Int,
            seriesId: Int,
        ): Book = PreviewData.baseBook.copy(
            id = id,
            bookSeries = BookSeries(
                id = seriesId,
                name = "Test Series",
                amountOfBooks = 10,
            ),
        )

        @Test
        fun `dismissing a series drops its book while the refetch is still suspended`() = runTest {
            // ----- Arrange -----
            val readBook = bookInSeries(
                id = 1,
                seriesId = 3000,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )
            val nextBook = fetchedBook(
                id = 3002,
                seriesId = 3000,
            )
            val dismissedSeriesIds = MutableStateFlow(emptyList<Int>())
            val pendingRefetch = CompletableDeferred<List<Book>>()

            every {
                booksRepository.books
            } returns flowOf(listOf(readBook))
            every {
                exploreRepository.dismissedContinueSeriesIds
            } returns dismissedSeriesIds
            coEvery {
                exploreRepository.fetchNextBooksInSeries(
                    seeds = listOf(
                        SeriesContinuationSeed(
                            seriesId = 3000,
                            afterPosition = 1.0,
                        ),
                    ),
                )
            } returns listOf(nextBook)
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            } coAnswers { pendingRefetch.await() }

            useCase().test {
                awaitItem() shouldBe listOf(nextBook)

                // ----- Act -----
                dismissedSeriesIds.value = listOf(3000)

                // ----- Assert -----
                awaitItem() shouldBe emptyList()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `dismissing a single book drops it while the refetch is still suspended`() = runTest {
            // ----- Arrange -----
            val readBook = bookInSeries(
                id = 1,
                seriesId = 3100,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )
            val nextBook = fetchedBook(
                id = 3102,
                seriesId = 3100,
            )
            val dismissedBooks = MutableStateFlow(emptyList<DismissedSeriesBook>())
            val pendingRefetch = CompletableDeferred<List<Book>>()

            every {
                booksRepository.books
            } returns flowOf(listOf(readBook))
            every {
                exploreRepository.dismissedContinueSeriesBooks
            } returns dismissedBooks
            coEvery {
                exploreRepository.fetchNextBooksInSeries(
                    seeds = listOf(
                        SeriesContinuationSeed(
                            seriesId = 3100,
                            afterPosition = 1.0,
                        ),
                    ),
                )
            } returns listOf(nextBook)
            coEvery {
                exploreRepository.fetchNextBooksInSeries(
                    seeds = listOf(
                        SeriesContinuationSeed(
                            seriesId = 3100,
                            afterPosition = 2.0,
                        ),
                    ),
                )
            } coAnswers { pendingRefetch.await() }

            useCase().test {
                awaitItem() shouldBe listOf(nextBook)

                // ----- Act -----
                dismissedBooks.value = listOf(
                    dismissedBook(
                        bookId = 3102,
                        seriesId = 3100,
                        seriesPosition = 2.0,
                    ),
                )

                // ----- Assert -----
                awaitItem() shouldBe emptyList()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `undoing a series dismissal brings its book back once the refetch returns`() = runTest {
            // ----- Arrange -----
            val readBook = bookInSeries(
                id = 1,
                seriesId = 3200,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )
            val nextBook = fetchedBook(
                id = 3202,
                seriesId = 3200,
            )
            val dismissedSeriesIds = MutableStateFlow(emptyList<Int>())

            every {
                booksRepository.books
            } returns flowOf(listOf(readBook))
            every {
                exploreRepository.dismissedContinueSeriesIds
            } returns dismissedSeriesIds
            coEvery {
                exploreRepository.fetchNextBooksInSeries(
                    seeds = listOf(
                        SeriesContinuationSeed(
                            seriesId = 3200,
                            afterPosition = 1.0,
                        ),
                    ),
                )
            } returns listOf(nextBook)
            coEvery {
                exploreRepository.fetchNextBooksInSeries(seeds = emptyList())
            } returns emptyList()

            useCase().test {
                awaitItem() shouldBe listOf(nextBook)
                dismissedSeriesIds.value = listOf(3200)
                var latest = awaitItem()
                while (latest.isNotEmpty()) {
                    latest = awaitItem()
                }

                // ----- Act -----
                dismissedSeriesIds.value = emptyList()

                // ----- Assert -----
                var restored = awaitItem()
                while (restored.isEmpty()) {
                    restored = awaitItem()
                }
                restored shouldBe listOf(nextBook)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
