package nl.rhaydus.softcover.feature.explore.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesEntity
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.data.datasource.DismissedContinueSeriesLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchRemoteDataSource
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag


class ExploreRepositoryImplTest {
    private lateinit var searchRemoteDataSource: SearchRemoteDataSource
    private lateinit var searchLocalDataSource: SearchLocalDataSource
    private lateinit var dismissedContinueSeriesLocalDataSource: DismissedContinueSeriesLocalDataSource
    private lateinit var repository: ExploreRepositoryImpl

    @BeforeEach
    fun setUp() {
        searchRemoteDataSource = mockk(relaxed = true)
        searchLocalDataSource = mockk(relaxed = true)
        dismissedContinueSeriesLocalDataSource = mockk(relaxed = true)

        repository = ExploreRepositoryImpl(
            searchRemoteDataSource = searchRemoteDataSource,
            searchLocalDataSource = searchLocalDataSource,
            dismissedContinueSeriesLocalDataSource = dismissedContinueSeriesLocalDataSource,
        )
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class PreviousSearchQueries {
        @Test
        fun `previousSearchQueries is wired to local data source`() = runTest {
            // ----- Arrange -----
            val expectedQueries = listOf("kotlin", "android")

            every {
                searchLocalDataSource.previousSearchQueries
            } returns flowOf(expectedQueries)

            val freshRepository = ExploreRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
                dismissedContinueSeriesLocalDataSource = dismissedContinueSeriesLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.previousSearchQueries.test {
                awaitItem() shouldBe expectedQueries
                awaitComplete()
            }
        }

        @Test
        fun `previousSearchQueries emits empty list when local data source emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                searchLocalDataSource.previousSearchQueries
            } returns flowOf(emptyList())

            val freshRepository = ExploreRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
                dismissedContinueSeriesLocalDataSource = dismissedContinueSeriesLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.previousSearchQueries.test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }

    @Nested
    inner class QueriedBooks {
        @Test
        fun `queriedBooks is wired to remote data source`() = runTest {
            // ----- Arrange -----
            val expectedBooks = listOf(stubBook())

            every {
                searchRemoteDataSource.queriedBooks
            } returns flowOf(expectedBooks)

            val freshRepository = ExploreRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
                dismissedContinueSeriesLocalDataSource = dismissedContinueSeriesLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.queriedBooks.test {
                awaitItem() shouldBe expectedBooks
                awaitComplete()
            }
        }
    }

    @Nested
    inner class QueriedBooksHasMore {
        @Test
        fun `queriedBooksHasMore is wired to remote data source`() = runTest {
            // ----- Arrange -----
            every {
                searchRemoteDataSource.queriedBooksHasMore
            } returns flowOf(true)

            val freshRepository = ExploreRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
                dismissedContinueSeriesLocalDataSource = dismissedContinueSeriesLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.queriedBooksHasMore.test {
                awaitItem() shouldBe true
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SearchForName {
        @Test
        fun `delegates to remote data source with correct arguments`() = runTest {
            // ----- Arrange -----
            val name = "dune"
            val userId = 5

            // ----- Act -----
            repository.searchForName(
                name = name,
                userId = userId,
                sortMode = ExploreSortMode.POPULARITY,
            )

            // ----- Assert -----
            coVerify {
                searchRemoteDataSource.searchForName(
                    name = name,
                    userId = userId,
                    sortMode = ExploreSortMode.POPULARITY,
                )
            }
        }
    }

    @Nested
    inner class FetchBooksByGenre {
        @Test
        fun `delegates to remote data source and returns the mapped result`() = runTest {
            // ----- Arrange -----
            val genre = "Fantasy"
            val limit = 50
            val expectedBooks = listOf(stubBook())

            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = genre,
                    limit = limit,
                )
            } returns expectedBooks

            // ----- Act -----
            val result = repository.fetchBooksByGenre(
                genre = genre,
                limit = limit,
            )

            // ----- Assert -----
            result shouldBe expectedBooks
            coVerify {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = genre,
                    limit = limit,
                )
            }
        }

        @Test
        fun `caches by genre so a second call for the same genre does not hit remote again`() = runTest {
            // ----- Arrange -----
            val genre = "Fantasy"
            val limit = 50
            val expectedBooks = listOf(stubBook())

            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = genre,
                    limit = limit,
                )
            } returns expectedBooks

            // ----- Act -----
            val firstResult = repository.fetchBooksByGenre(
                genre = genre,
                limit = limit,
            )
            val secondResult = repository.fetchBooksByGenre(
                genre = genre,
                limit = limit,
            )

            // ----- Assert -----
            firstResult shouldBe expectedBooks
            secondResult shouldBe expectedBooks
            coVerify(exactly = 1) {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = genre,
                    limit = limit,
                )
            }
        }

        @Test
        fun `caches independently for two different genres`() = runTest {
            // ----- Arrange -----
            val fantasyGenre = "Fantasy"
            val horrorGenre = "Horror"
            val limit = 50
            val fantasyBooks = listOf(stubBook())
            val horrorBooks = listOf(stubBook())

            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = fantasyGenre,
                    limit = limit,
                )
            } returns fantasyBooks
            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = horrorGenre,
                    limit = limit,
                )
            } returns horrorBooks

            // ----- Act -----
            val fantasyResult = repository.fetchBooksByGenre(
                genre = fantasyGenre,
                limit = limit,
            )
            val horrorResult = repository.fetchBooksByGenre(
                genre = horrorGenre,
                limit = limit,
            )

            // ----- Assert -----
            fantasyResult shouldBe fantasyBooks
            horrorResult shouldBe horrorBooks
            coVerify(exactly = 1) {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = fantasyGenre,
                    limit = limit,
                )
            }
            coVerify(exactly = 1) {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = horrorGenre,
                    limit = limit,
                )
            }
        }

        @Test
        fun `re-requesting the first genre after fetching a second genre still hits cache`() = runTest {
            // ----- Arrange -----
            val fantasyGenre = "Fantasy"
            val horrorGenre = "Horror"
            val limit = 50
            val fantasyBooks = listOf(stubBook())
            val horrorBooks = listOf(stubBook())

            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = fantasyGenre,
                    limit = limit,
                )
            } returns fantasyBooks
            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = horrorGenre,
                    limit = limit,
                )
            } returns horrorBooks

            // ----- Act -----
            repository.fetchBooksByGenre(
                genre = fantasyGenre,
                limit = limit,
            )
            repository.fetchBooksByGenre(
                genre = horrorGenre,
                limit = limit,
            )
            val secondFantasyResult = repository.fetchBooksByGenre(
                genre = fantasyGenre,
                limit = limit,
            )

            // ----- Assert -----
            secondFantasyResult shouldBe fantasyBooks
            coVerify(exactly = 1) {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = fantasyGenre,
                    limit = limit,
                )
            }
        }

        @Test
        fun `a failed fetch for one genre does not affect an already-cached different genre`() = runTest {
            // ----- Arrange -----
            val fantasyGenre = "Fantasy"
            val horrorGenre = "Horror"
            val limit = 50
            val fantasyBooks = listOf(stubBook())

            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = fantasyGenre,
                    limit = limit,
                )
            } returns fantasyBooks
            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = horrorGenre,
                    limit = limit,
                )
            } throws RuntimeException("boom")

            // ----- Act -----
            repository.fetchBooksByGenre(
                genre = fantasyGenre,
                limit = limit,
            )
            val horrorFailure = runCatching { repository.fetchBooksByGenre(
                genre = horrorGenre,
                limit = limit,
            ) }
            val fantasyResult = repository.fetchBooksByGenre(
                genre = fantasyGenre,
                limit = limit,
            )

            // ----- Assert -----
            horrorFailure.isFailure shouldBe true
            fantasyResult shouldBe fantasyBooks
            coVerify(exactly = 1) {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = fantasyGenre,
                    limit = limit,
                )
            }
        }

        @Test
        fun `a cache hit is served regardless of the limit passed on a later call`() = runTest {
            // ----- Arrange -----
            val genre = "Fantasy"
            val firstLimit = 50
            val secondLimit = 10
            val expectedBooks = listOf(stubBook())

            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = genre,
                    limit = firstLimit,
                )
            } returns expectedBooks

            // ----- Act -----
            repository.fetchBooksByGenre(
                genre = genre,
                limit = firstLimit,
            )
            val secondResult = repository.fetchBooksByGenre(
                genre = genre,
                limit = secondLimit,
            )

            // ----- Assert -----
            secondResult shouldBe expectedBooks
            coVerify(exactly = 1) {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = genre,
                    limit = firstLimit,
                )
            }
            coVerify(exactly = 0) {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = genre,
                    limit = secondLimit,
                )
            }
        }
    }

    @Nested
    inner class FetchFeaturedUpcomingRelease {
        @Test
        fun `two calls hit remote exactly once and both return the same value`() = runTest {
            // ----- Arrange -----
            val expectedBook = stubBook()
            coEvery {
                searchRemoteDataSource.fetchFeaturedUpcomingRelease()
            } returns expectedBook

            // ----- Act -----
            val firstResult = repository.fetchFeaturedUpcomingRelease()
            val secondResult = repository.fetchFeaturedUpcomingRelease()

            // ----- Assert -----
            firstResult shouldBe expectedBook
            secondResult shouldBe expectedBook
            coVerify(exactly = 1) { searchRemoteDataSource.fetchFeaturedUpcomingRelease() }
        }

        @Test
        fun `a null result is cached and not re-fetched`() = runTest {
            // ----- Arrange -----
            coEvery {
                searchRemoteDataSource.fetchFeaturedUpcomingRelease()
            } returns null

            // ----- Act -----
            val firstResult = repository.fetchFeaturedUpcomingRelease()
            val secondResult = repository.fetchFeaturedUpcomingRelease()

            // ----- Assert -----
            firstResult shouldBe null
            secondResult shouldBe null
            coVerify(exactly = 1) { searchRemoteDataSource.fetchFeaturedUpcomingRelease() }
        }

        @Test
        fun `a failed fetch is not cached and the next call retries`() = runTest {
            // ----- Arrange -----
            val expectedBook = stubBook()
            coEvery {
                searchRemoteDataSource.fetchFeaturedUpcomingRelease()
            } throws RuntimeException("boom") andThen expectedBook

            // ----- Act -----
            val firstAttempt = runCatching { repository.fetchFeaturedUpcomingRelease() }
            val secondResult = repository.fetchFeaturedUpcomingRelease()

            // ----- Assert -----
            firstAttempt.isFailure shouldBe true
            secondResult shouldBe expectedBook
            coVerify(exactly = 2) { searchRemoteDataSource.fetchFeaturedUpcomingRelease() }
        }
    }

    @Nested
    inner class FetchMoodTags {
        @Test
        fun `two calls return the same list and remote is hit exactly once`() = runTest {
            // ----- Arrange -----
            val expectedMoodTags = listOf<MoodTag>(mockk())
            coEvery {
                searchRemoteDataSource.fetchMoodTags(limit = any())
            } returns expectedMoodTags

            // ----- Act -----
            val firstResult = repository.fetchMoodTags()
            val secondResult = repository.fetchMoodTags()

            // ----- Assert -----
            firstResult shouldBe expectedMoodTags
            secondResult shouldBe expectedMoodTags
            coVerify(exactly = 1) { searchRemoteDataSource.fetchMoodTags(limit = any()) }
        }

        @Test
        fun `a failed fetch is not cached and the next call retries`() = runTest {
            // ----- Arrange -----
            val expectedMoodTags = listOf<MoodTag>(mockk())
            coEvery {
                searchRemoteDataSource.fetchMoodTags(limit = any())
            } throws RuntimeException("boom") andThen expectedMoodTags

            // ----- Act -----
            val firstAttempt = runCatching { repository.fetchMoodTags() }
            val secondResult = repository.fetchMoodTags()

            // ----- Assert -----
            firstAttempt.isFailure shouldBe true
            secondResult shouldBe expectedMoodTags
            coVerify(exactly = 2) { searchRemoteDataSource.fetchMoodTags(limit = any()) }
        }

        @Test
        fun `an empty list is cached and not re-fetched`() = runTest {
            // ----- Arrange -----
            coEvery {
                searchRemoteDataSource.fetchMoodTags(limit = any())
            } returns emptyList()

            // ----- Act -----
            val firstResult = repository.fetchMoodTags()
            val secondResult = repository.fetchMoodTags()

            // ----- Assert -----
            firstResult shouldBe emptyList()
            secondResult shouldBe emptyList()
            coVerify(exactly = 1) { searchRemoteDataSource.fetchMoodTags(limit = any()) }
        }
    }

    @Nested
    inner class ClearSearchResults {
        @Test
        fun `delegates to remote data source`() = runTest {
            // ----- Arrange -----
            // (searchRemoteDataSource is relaxed — no additional setup needed)

            // ----- Act -----
            repository.clearSearchResults()

            // ----- Assert -----
            coVerify {
                searchRemoteDataSource.clearSearchResults()
            }
        }
    }

    @Nested
    inner class SaveSearchQuery {
        @Test
        fun `delegates to local data source with correct name`() = runTest {
            // ----- Arrange -----
            val name = "foundation"

            // ----- Act -----
            repository.saveSearchQuery(name = name)

            // ----- Assert -----
            coVerify {
                searchLocalDataSource.saveSearchQuery(name = name)
            }
        }
    }

    @Nested
    inner class RemoveSearchQuery {
        @Test
        fun `delegates to local data source with correct name`() = runTest {
            // ----- Arrange -----
            val name = "neuromancer"

            // ----- Act -----
            repository.removeSearchQuery(name = name)

            // ----- Assert -----
            coVerify {
                searchLocalDataSource.removeSearchQuery(name = name)
            }
        }
    }

    @Nested
    inner class RemoveAllSearchQueries {
        @Test
        fun `delegates to local data source`() = runTest {
            // ----- Arrange -----
            // (searchLocalDataSource is relaxed — no additional setup needed)

            // ----- Act -----
            repository.removeAllSearchQueries()

            // ----- Assert -----
            coVerify {
                searchLocalDataSource.removeAllSearchQueries()
            }
        }
    }

    @Nested
    inner class DismissedContinueSeriesIds {
        @Test
        fun `is wired to dismissedContinueSeriesLocalDataSource dismissedSeriesIds`() = runTest {
            // ----- Arrange -----
            val expectedIds = listOf(100, 200)

            every {
                dismissedContinueSeriesLocalDataSource.dismissedSeriesIds
            } returns flowOf(expectedIds)

            val freshRepository = ExploreRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
                dismissedContinueSeriesLocalDataSource = dismissedContinueSeriesLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.dismissedContinueSeriesIds.test {
                awaitItem() shouldBe expectedIds
                awaitComplete()
            }
        }
    }

    @Nested
    inner class DismissContinueSeriesBook {
        @Test
        fun `delegates to dismissedContinueSeriesLocalDataSource with an entity mapped from the book`() = runTest {
            // ----- Arrange -----
            val book = DismissedSeriesBook(
                bookId = 42,
                title = "Dune",
                coverUrl = "https://example.com/dune.jpg",
                authorText = "Frank Herbert",
                seriesName = "Dune Saga",
                seriesId = 7,
                seriesPosition = 2.0,
            )
            val entitySlot = slot<DismissedContinueSeriesBookEntity>()

            // ----- Act -----
            repository.dismissContinueSeriesBook(book = book)

            // ----- Assert -----
            coVerify {
                dismissedContinueSeriesLocalDataSource.dismissBook(entity = capture(entitySlot))
            }
            entitySlot.captured shouldBe DismissedContinueSeriesBookEntity(
                bookId = book.bookId,
                bookTitle = book.title,
                coverUrl = book.coverUrl,
                authorText = book.authorText,
                seriesName = book.seriesName,
                seriesId = book.seriesId,
                seriesPosition = book.seriesPosition,
            )
        }

        @Test
        fun `carries the series cursor through so the series can advance past the hidden book`() = runTest {
            // ----- Arrange -----
            val book = DismissedSeriesBook(
                bookId = 43,
                title = null,
                coverUrl = null,
                authorText = null,
                seriesName = null,
                seriesId = 9,
                seriesPosition = 5.0,
            )
            val entitySlot = slot<DismissedContinueSeriesBookEntity>()

            // ----- Act -----
            repository.dismissContinueSeriesBook(book = book)

            // ----- Assert -----
            coVerify {
                dismissedContinueSeriesLocalDataSource.dismissBook(entity = capture(entitySlot))
            }
            entitySlot.captured.seriesId shouldBe book.seriesId
            entitySlot.captured.seriesPosition shouldBe book.seriesPosition
        }
    }

    @Nested
    inner class DismissContinueSeries {
        @Test
        fun `delegates to dismissedContinueSeriesLocalDataSource with correct arguments`() = runTest {
            // ----- Arrange -----
            val seriesId = 99
            val seriesName = "Foundation"
            val coverUrl = "https://example.com/foundation.jpg"
            val authorText = "Isaac Asimov"
            val bookCount = 7

            // ----- Act -----
            repository.dismissContinueSeries(
                seriesId = seriesId,
                seriesName = seriesName,
                coverUrl = coverUrl,
                authorText = authorText,
                bookCount = bookCount,
            )

            // ----- Assert -----
            coVerify {
                dismissedContinueSeriesLocalDataSource.dismissSeries(
                    seriesId = seriesId,
                    seriesName = seriesName,
                    coverUrl = coverUrl,
                    authorText = authorText,
                    bookCount = bookCount,
                )
            }
        }

        @Test
        fun `delegates to dismissedContinueSeriesLocalDataSource with null metadata defaults`() = runTest {
            // ----- Arrange -----
            val seriesId = 100

            // ----- Act -----
            repository.dismissContinueSeries(seriesId = seriesId)

            // ----- Assert -----
            coVerify {
                dismissedContinueSeriesLocalDataSource.dismissSeries(
                    seriesId = seriesId,
                    seriesName = null,
                    coverUrl = null,
                    authorText = null,
                    bookCount = null,
                )
            }
        }
    }

    @Nested
    inner class DismissedContinueSeriesBooks {
        @Test
        fun `maps entities from dismissedContinueSeriesLocalDataSource dismissedBooks to domain models`() = runTest {
            // ----- Arrange -----
            val entities = listOf(
                DismissedContinueSeriesBookEntity(
                    bookId = 1,
                    bookTitle = "Dune",
                    coverUrl = "cover.jpg",
                    authorText = "Frank Herbert",
                    seriesName = "Dune Saga",
                    seriesId = 7,
                    seriesPosition = 2.0,
                ),
            )

            every {
                dismissedContinueSeriesLocalDataSource.dismissedBooks
            } returns flowOf(entities)

            val freshRepository = ExploreRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
                dismissedContinueSeriesLocalDataSource = dismissedContinueSeriesLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.dismissedContinueSeriesBooks.test {
                awaitItem() shouldBe listOf(
                    DismissedSeriesBook(
                        bookId = 1,
                        title = "Dune",
                        coverUrl = "cover.jpg",
                        authorText = "Frank Herbert",
                        seriesName = "Dune Saga",
                        seriesId = 7,
                        seriesPosition = 2.0,
                    ),
                )
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when dismissedBooks emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                dismissedContinueSeriesLocalDataSource.dismissedBooks
            } returns flowOf(emptyList())

            val freshRepository = ExploreRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
                dismissedContinueSeriesLocalDataSource = dismissedContinueSeriesLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.dismissedContinueSeriesBooks.test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }

    @Nested
    inner class DismissedContinueSeriesCollection {
        @Test
        fun `maps entities from dismissedContinueSeriesLocalDataSource dismissedSeries to domain models`() = runTest {
            // ----- Arrange -----
            val entities = listOf(
                DismissedContinueSeriesEntity(
                    seriesId = 10,
                    seriesName = "Foundation",
                    coverUrl = "cover.jpg",
                ),
            )

            every {
                dismissedContinueSeriesLocalDataSource.dismissedSeries
            } returns flowOf(entities)

            val freshRepository = ExploreRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
                dismissedContinueSeriesLocalDataSource = dismissedContinueSeriesLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.dismissedContinueSeries.test {
                awaitItem() shouldBe listOf(
                    DismissedSeries(
                        seriesId = 10,
                        seriesName = "Foundation",
                        coverUrl = "cover.jpg",
                        authorText = null,
                        bookCount = null,
                    ),
                )
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when dismissedSeries emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                dismissedContinueSeriesLocalDataSource.dismissedSeries
            } returns flowOf(emptyList())

            val freshRepository = ExploreRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
                dismissedContinueSeriesLocalDataSource = dismissedContinueSeriesLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.dismissedContinueSeries.test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }

    @Nested
    inner class UndoContinueSeriesBookDismissal {
        @Test
        fun `delegates to dismissedContinueSeriesLocalDataSource with correct bookId`() = runTest {
            // ----- Arrange -----
            val bookId = 77

            // ----- Act -----
            repository.undoContinueSeriesBookDismissal(bookId = bookId)

            // ----- Assert -----
            coVerify {
                dismissedContinueSeriesLocalDataSource.undoBookDismissal(bookId = bookId)
            }
        }
    }

    @Nested
    inner class UndoContinueSeriesDismissal {
        @Test
        fun `delegates to dismissedContinueSeriesLocalDataSource with correct seriesId`() = runTest {
            // ----- Arrange -----
            val seriesId = 55

            // ----- Act -----
            repository.undoContinueSeriesDismissal(seriesId = seriesId)

            // ----- Assert -----
            coVerify {
                dismissedContinueSeriesLocalDataSource.undoSeriesDismissal(seriesId = seriesId)
            }
        }
    }
}
