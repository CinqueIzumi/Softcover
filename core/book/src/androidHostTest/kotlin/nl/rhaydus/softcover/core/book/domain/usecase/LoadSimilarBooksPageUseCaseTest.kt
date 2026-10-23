package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookCandidate
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookSeed
import nl.rhaydus.softcover.core.book.domain.repository.SimilarBooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LoadSimilarBooksPageUseCaseTest {
    private lateinit var similarBooksRepository: SimilarBooksRepository
    private lateinit var useCase: LoadSimilarBooksPageUseCase

    @BeforeEach
    fun setUp() {
        similarBooksRepository = mockk()
        useCase = LoadSimilarBooksPageUseCase(similarBooksRepository = similarBooksRepository)
    }

    private fun buildBook(
        id: Int,
        canonicalId: Int? = null,
    ): Book = Book(
        id = id,
        canonicalId = canonicalId,
        title = "Title",
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
    )

    private fun buildCandidate(
        id: Int,
        canonicalId: Int? = null,
        seriesIds: Set<Int> = emptySet(),
    ): SimilarBookCandidate = SimilarBookCandidate(
        book = buildBook(
            id = id,
            canonicalId = canonicalId,
        ),
        seriesIds = seriesIds,
    )

    private fun buildSeed(
        rankedIds: List<Int>,
        sourceBookId: Int = SOURCE_ID,
        sourceCanonicalId: Int? = null,
        excludedSeriesBookIds: Set<Int> = emptySet(),
        sourceSeriesIds: Set<Int> = emptySet(),
    ): SimilarBookSeed = SimilarBookSeed(
        sourceBookId = sourceBookId,
        sourceCanonicalId = sourceCanonicalId,
        rankedIds = rankedIds,
        excludedSeriesBookIds = excludedSeriesBookIds,
        sourceSeriesIds = sourceSeriesIds,
    )

    private fun stubRepository(
        seed: SimilarBookSeed,
        shelved: Set<Int> = emptySet(),
        candidates: List<SimilarBookCandidate> = emptyList(),
    ) {
        coEvery {
            similarBooksRepository.getSeed(bookId = SOURCE_ID)
        } returns seed
        coEvery {
            similarBooksRepository.getShelvedBookIds(ids = any())
        } answers {
            firstArg<List<Int>>().filter { it in shelved }.toSet()
        }
        coEvery {
            similarBooksRepository.fetchCandidates(ids = any())
        } returns candidates
    }

    @Nested
    inner class Windowing {
        @Test
        fun `skips source, same-series and shelved ids but still advances past them`() = runTest {
            // ----- Arrange -----
            val seed = buildSeed(
                rankedIds = listOf(SOURCE_ID, 2, 3, 4, 5),
                excludedSeriesBookIds = setOf(3),
            )
            stubRepository(
                seed = seed,
                shelved = setOf(4),
                candidates = listOf(buildCandidate(id = 2), buildCandidate(id = 5)),
            )

            // ----- Act -----
            val result = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
                pageSize = 10,
            )

            // ----- Assert -----
            result.getOrThrow().nextOffset shouldBe 5
            coVerify(exactly = 1) { similarBooksRepository.fetchCandidates(ids = listOf(2, 5)) }
        }

        @Test
        fun `starts at the offset and stops once the page is full`() = runTest {
            // ----- Arrange -----
            stubRepository(
                seed = buildSeed(rankedIds = listOf(2, 3, 4, 5, 6)),
                candidates = listOf(buildCandidate(id = 4), buildCandidate(id = 5)),
            )

            // ----- Act -----
            val page = useCase(
                sourceBookId = SOURCE_ID,
                offset = 2,
                pageSize = 2,
            ).getOrThrow()

            // ----- Assert -----
            page.nextOffset shouldBe 4
            page.hasMore shouldBe true
            coVerify(exactly = 1) { similarBooksRepository.fetchCandidates(ids = listOf(4, 5)) }
        }

        @Test
        fun `reports no more once the end of the list is reached`() = runTest {
            // ----- Arrange -----
            stubRepository(
                seed = buildSeed(rankedIds = listOf(2, 3)),
                candidates = listOf(buildCandidate(id = 2), buildCandidate(id = 3)),
            )

            // ----- Act -----
            val page = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
                pageSize = 2,
            ).getOrThrow()

            // ----- Assert -----
            page.hasMore shouldBe false
            page.nextOffset shouldBe 2
        }

        @Test
        fun `makes no candidate request when the window is empty`() = runTest {
            // ----- Arrange -----
            stubRepository(
                seed = buildSeed(rankedIds = listOf(2)),
                shelved = setOf(2),
            )

            // ----- Act -----
            val page = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
            ).getOrThrow()

            // ----- Assert -----
            page.books.shouldBeEmpty()
            page.hasMore shouldBe false
            coVerify(exactly = 0) { similarBooksRepository.fetchCandidates(ids = any()) }
        }
    }

    @Nested
    inner class Filtering {
        @Test
        fun `restores rank order from unordered hydration`() = runTest {
            // ----- Arrange -----
            stubRepository(
                seed = buildSeed(rankedIds = listOf(2, 3, 4)),
                candidates = listOf(
                    buildCandidate(id = 4),
                    buildCandidate(id = 2),
                    buildCandidate(id = 3),
                ),
            )

            // ----- Act -----
            val page = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
            ).getOrThrow()

            // ----- Assert -----
            page.books.map { it.id } shouldBe listOf(2, 3, 4)
        }

        @Test
        fun `drops candidates sharing a series with the source`() = runTest {
            // ----- Arrange -----
            stubRepository(
                seed = buildSeed(
                    rankedIds = listOf(2, 3),
                    sourceSeriesIds = setOf(9),
                ),
                candidates = listOf(
                    buildCandidate(
                        id = 2,
                        seriesIds = setOf(1, 9),
                    ),
                    buildCandidate(
                        id = 3,
                        seriesIds = setOf(1),
                    ),
                ),
            )

            // ----- Act -----
            val page = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
            ).getOrThrow()

            // ----- Assert -----
            page.books.map { it.id } shouldBe listOf(3)
        }

        @Test
        fun `drops candidates whose canonical id is the source or its canonical`() = runTest {
            // ----- Arrange -----
            stubRepository(
                seed = buildSeed(
                    rankedIds = listOf(2, 3, 4),
                    sourceCanonicalId = 77,
                ),
                candidates = listOf(
                    buildCandidate(
                        id = 2,
                        canonicalId = SOURCE_ID,
                    ),
                    buildCandidate(
                        id = 3,
                        canonicalId = 77,
                    ),
                    buildCandidate(id = 4),
                ),
            )

            // ----- Act -----
            val page = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
            ).getOrThrow()

            // ----- Assert -----
            page.books.map { it.id } shouldBe listOf(4)
        }

        @Test
        fun `keeps only the first of candidates sharing a canonical id`() = runTest {
            // ----- Arrange -----
            stubRepository(
                seed = buildSeed(rankedIds = listOf(2, 3, 50)),
                candidates = listOf(
                    buildCandidate(
                        id = 2,
                        canonicalId = 50,
                    ),
                    buildCandidate(
                        id = 3,
                        canonicalId = 50,
                    ),
                    buildCandidate(id = 50),
                ),
            )

            // ----- Act -----
            val page = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
            ).getOrThrow()

            // ----- Assert -----
            page.books.map { it.id } shouldBe listOf(2)
        }

        @Test
        fun `drops candidates whose canonical id is shelved`() = runTest {
            // ----- Arrange -----
            stubRepository(
                seed = buildSeed(rankedIds = listOf(2, 3)),
                shelved = setOf(60),
                candidates = listOf(
                    buildCandidate(
                        id = 2,
                        canonicalId = 60,
                    ),
                    buildCandidate(id = 3),
                ),
            )

            // ----- Act -----
            val page = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
            ).getOrThrow()

            // ----- Assert -----
            page.books.map { it.id } shouldBe listOf(3)
        }

        @Test
        fun `returns empty books with more available when every candidate is filtered`() = runTest {
            // ----- Arrange -----
            stubRepository(
                seed = buildSeed(
                    rankedIds = listOf(2, 3, 4),
                    sourceSeriesIds = setOf(9),
                ),
                candidates = listOf(
                    buildCandidate(
                        id = 2,
                        seriesIds = setOf(9),
                    ),
                ),
            )

            // ----- Act -----
            val page = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
                pageSize = 1,
            ).getOrThrow()

            // ----- Assert -----
            page.books.shouldBeEmpty()
            page.hasMore shouldBe true
            page.nextOffset shouldBe 1
        }
    }

    @Nested
    inner class Failures {
        @Test
        fun `returns failure when the seed cannot be fetched`() = runTest {
            // ----- Arrange -----
            val error = IllegalStateException("boom")
            coEvery {
                similarBooksRepository.getSeed(bookId = SOURCE_ID)
            } throws error

            // ----- Act -----
            val result = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
            )

            // ----- Assert -----
            result.exceptionOrNull().shouldNotBeNull() shouldBe error
        }

        @Test
        fun `returns failure when hydration fails`() = runTest {
            // ----- Arrange -----
            val error = IllegalStateException("boom")
            stubRepository(seed = buildSeed(rankedIds = listOf(2)))
            coEvery {
                similarBooksRepository.fetchCandidates(ids = any())
            } throws error

            // ----- Act -----
            val result = useCase(
                sourceBookId = SOURCE_ID,
                offset = 0,
            )

            // ----- Assert -----
            result.exceptionOrNull().shouldNotBeNull() shouldBe error
        }
    }

    private companion object {
        const val SOURCE_ID = 1
    }
}
