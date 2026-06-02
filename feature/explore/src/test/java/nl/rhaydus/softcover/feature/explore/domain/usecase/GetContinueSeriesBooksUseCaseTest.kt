package nl.rhaydus.softcover.feature.explore.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
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

        every { exploreRepository.dismissedContinueSeriesBookIds } returns flowOf(emptyList())
        every { exploreRepository.dismissedContinueSeriesIds } returns flowOf(emptyList())

        useCase = GetContinueSeriesBooksUseCase(
            booksRepository = booksRepository,
            exploreRepository = exploreRepository,
        )
    }

    private fun stubUserBook(status: BookStatus): UserBook = mockk {
        every { this@mockk.status } returns status
    }

    private fun bookInSeries(
        id: Int,
        seriesId: Int,
        seriesName: String = "Test Series",
        amountOfBooks: Int = 10,
        positionsInSeries: List<Double> = emptyList(),
        status: BookStatus,
    ): Book = PreviewData.baseBook.copy(
        id = id,
        bookSeries = BookSeries(
            id = seriesId,
            name = seriesName,
            amountOfBooks = amountOfBooks,
        ),
        positionsInSeries = positionsInSeries,
        userBook = stubUserBook(status),
        userBookRead = null,
    )

    // ----- Invoke -----

    @Nested
    inner class Invoke {
        @Test
        fun `returns empty list when books flow emits empty list`() = runTest {
            // ----- Arrange -----
            every { booksRepository.books } returns flowOf(emptyList())

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

            every { booksRepository.books } returns flowOf(listOf(dnfBook, readBook))

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

            every { booksRepository.books } returns flowOf(listOf(lowerBook, higherBook))

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 200,
                    afterPosition = 4.0,
                )
            } returns nextBook

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
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

            every { booksRepository.books } returns flowOf(listOf(finalBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) { exploreRepository.fetchNextInSeries(
                any(),
                any(),
            ) }
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

            every { booksRepository.books } returns flowOf(listOf(wantToReadBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) { exploreRepository.fetchNextInSeries(
                any(),
                any(),
            ) }
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

            every { booksRepository.books } returns flowOf(listOf(noneBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) { exploreRepository.fetchNextInSeries(
                any(),
                any(),
            ) }
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

            every { booksRepository.books } returns flowOf(listOf(noSeriesBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) { exploreRepository.fetchNextInSeries(
                any(),
                any(),
            ) }
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

            every { booksRepository.books } returns flowOf(listOf(noPositionBook))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) { exploreRepository.fetchNextInSeries(
                any(),
                any(),
            ) }
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

            every { booksRepository.books } returns flowOf(listOf(book))

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 700,
                    afterPosition = 1.0,
                )
            } returns nextBook

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
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

            every { booksRepository.books } returns flowOf(listOf(book))

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 800,
                    afterPosition = 1.5,
                )
            } returns nextBook

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
        }

        @Test
        fun `fan-out calls fetchNextInSeries once per qualifying series and collects all results`() = runTest {
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

            every { booksRepository.books } returns flowOf(listOf(bookA, bookB))

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 901,
                    afterPosition = 1.0,
                )
            } returns nextA

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 902,
                    afterPosition = 2.0,
                )
            } returns nextB

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result.toSet() shouldBe setOf(nextA, nextB)
        }

        @Test
        fun `fetch failure for one series does not prevent other series results from returning`() = runTest {
            // ----- Arrange -----
            val bookA = bookInSeries(
                id = 1,
                seriesId = 1001,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )
            val bookB = bookInSeries(
                id = 2,
                seriesId = 1002,
                amountOfBooks = 5,
                positionsInSeries = listOf(2.0),
                status = BookStatus.Read,
            )
            val nextB: Book = mockk(relaxed = true)

            every { booksRepository.books } returns flowOf(listOf(bookA, bookB))

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 1001,
                    afterPosition = 1.0,
                )
            } throws RuntimeException("network error")

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 1002,
                    afterPosition = 2.0,
                )
            } returns nextB

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextB)
        }

        @Test
        fun `null return from fetchNextInSeries is filtered out while non-null results are returned`() = runTest {
            // ----- Arrange -----
            val bookA = bookInSeries(
                id = 1,
                seriesId = 1101,
                amountOfBooks = 5,
                positionsInSeries = listOf(5.0),
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

            every { booksRepository.books } returns flowOf(listOf(bookA, bookB))

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 1101,
                    afterPosition = 5.0,
                )
            } returns null

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 1102,
                    afterPosition = 1.0,
                )
            } returns nextB

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextB)
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

            every { booksRepository.books } returns flowOf(listOf(compilation))

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 1200,
                    afterPosition = 3.0,
                )
            } returns nextBook

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
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

            every { booksRepository.books } returns flowOf(listOf(compilation))

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 1300,
                    afterPosition = 3.0,
                )
            } returns nextBook

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
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

            every { booksRepository.books } returns flowOf(listOf(compilation, singleton))

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 1400,
                    afterPosition = 3.0,
                )
            } returns nextBook

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe listOf(nextBook)
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

            every { booksRepository.books } returns flowOf(listOf(compilation))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) { exploreRepository.fetchNextInSeries(
                any(),
                any(),
            ) }
        }

        @Test
        fun `dismissed series id causes fetchNextInSeries to be skipped for that series`() = runTest {
            // ----- Arrange -----
            val book = bookInSeries(
                id = 1,
                seriesId = 1600,
                amountOfBooks = 5,
                positionsInSeries = listOf(1.0),
                status = BookStatus.Read,
            )

            every { booksRepository.books } returns flowOf(listOf(book))
            every { exploreRepository.dismissedContinueSeriesIds } returns flowOf(listOf(1600))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) { exploreRepository.fetchNextInSeries(
                any(),
                any(),
            ) }
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
                every { id } returns 999
            }

            every { booksRepository.books } returns flowOf(listOf(book))
            every { exploreRepository.dismissedContinueSeriesBookIds } returns flowOf(listOf(999))

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 1700,
                    afterPosition = 1.0,
                )
            } returns nextBook

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe emptyList()
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

            every { booksRepository.books } returns flowOf(listOf(book))
            every { exploreRepository.dismissedContinueSeriesIds } returns dismissedSeriesIds

            coEvery {
                exploreRepository.fetchNextInSeries(
                    seriesId = 1800,
                    afterPosition = 1.0,
                )
            } returns nextBook

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
}
