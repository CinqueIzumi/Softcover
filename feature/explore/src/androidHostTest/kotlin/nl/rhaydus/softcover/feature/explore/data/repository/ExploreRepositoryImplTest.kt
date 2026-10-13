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
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesEntity
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.data.datasource.DismissedContinueSeriesLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchRemoteDataSource
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
    inner class FetchBooksByGenre {
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
            val horrorFailure = runCatching {
                repository.fetchBooksByGenre(
                    genre = horrorGenre,
                    limit = limit,
                )
            }
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
    }

    @Nested
    inner class PeekBooksByGenre {
        @Test
        fun `returns null before any fetch for the genre`() = runTest {
            // ----- Arrange -----
            // Nothing fetched yet.

            // ----- Act -----
            val result = repository.peekBooksByGenre(genre = "Fantasy")

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns the fetched books after a fetch for that genre`() = runTest {
            // ----- Arrange -----
            val expectedBooks = listOf(stubBook())

            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = "Fantasy",
                    limit = 50,
                )
            } returns expectedBooks
            repository.fetchBooksByGenre(
                genre = "Fantasy",
                limit = 50,
            )

            // ----- Act -----
            val result = repository.peekBooksByGenre(genre = "Fantasy")

            // ----- Assert -----
            result shouldBe expectedBooks
        }

        @Test
        fun `returns null for a genre other than the one fetched`() = runTest {
            // ----- Arrange -----
            coEvery {
                searchRemoteDataSource.fetchBooksByGenre(
                    genre = "Fantasy",
                    limit = 50,
                )
            } returns listOf(stubBook())
            repository.fetchBooksByGenre(
                genre = "Fantasy",
                limit = 50,
            )

            // ----- Act -----
            val result = repository.peekBooksByGenre(genre = "Horror")

            // ----- Assert -----
            result shouldBe null
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
    }
}
