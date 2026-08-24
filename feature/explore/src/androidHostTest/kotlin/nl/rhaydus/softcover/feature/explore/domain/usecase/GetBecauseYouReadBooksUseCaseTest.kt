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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetBecauseYouReadGenreAsFlowUseCase
import nl.rhaydus.softcover.feature.explore.domain.model.BecauseYouReadRecommendation
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

// The use case's own GENRE_BOOKS_DISPLAY_LIMIT (private to that file) - duplicated here only to
// assert the overfetch limit passed to fetchBooksByGenre is comfortably past it.
private const val DISPLAY_LIMIT = 12

class GetBecauseYouReadBooksUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var listsRepository: ListsRepository
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var getBecauseYouReadGenreUseCase: GetBecauseYouReadGenreAsFlowUseCase
    private lateinit var useCase: GetBecauseYouReadBooksUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        listsRepository = mockk()
        exploreRepository = mockk()
        getBecauseYouReadGenreUseCase = mockk()

        // No lists by default - tests that care about list-based exclusion override this.
        every {
            listsRepository.allUserLists
        } returns flowOf(emptyList())

        // No override by default - existing tests exercise the auto-derived genre.
        every {
            getBecauseYouReadGenreUseCase()
        } returns flowOf(null)

        useCase = GetBecauseYouReadBooksUseCase(
            booksRepository = booksRepository,
            listsRepository = listsRepository,
            exploreRepository = exploreRepository,
            getBecauseYouReadGenreUseCase = getBecauseYouReadGenreUseCase,
        )
    }

    private fun stubBook(
        id: Int,
        genreTags: List<String> = emptyList(),
    ): Book = mockk {
        every {
            this@mockk.id
        } returns id

        // buildGenreOptions() reads every allUserBooks entry's tags - stub it so books passed
        // through booksRepository.books don't need every test to know about that internal.
        every {
            this@mockk.tags
        } returns genreTags.map { name ->
            Tag(
                id = 0,
                name = name,
                category = TagCategory.GENRE,
            )
        }
    }

    // Stands in for a user whose library has [count] books tagged with [genre] - buildGenreOptions()
    // ranks by how many books carry each genre tag, so this is how the default-genre derivation
    // sees a genre as "most-read".
    private fun booksTaggedWithGenre(
        genre: String,
        count: Int,
        startId: Int = 0,
    ): List<Book> = (0 until count).map { offset ->
        stubBook(
            id = startId + offset,
            genreTags = listOf(genre),
        )
    }

    // ----- Invoke -----

    @Nested
    inner class Invoke {
        @Test
        fun `emits null when the user has no books`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(emptyList())

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `emits null when no book carries a genre tag`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(listOf(stubBook(id = 1)))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `degrades to an empty book list instead of throwing when fetchBooksByGenre fails`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                ),
            )
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } throws RuntimeException("network error")

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.genre shouldBe "Romance"
            result?.books shouldBe emptyList()
        }

        @Test
        fun `requests an overfetch limit past the rail's display size`() = runTest {
            // ----- Arrange -----
            // Overfetching gives the client-side exclusion of owned/listed books (see the other
            // tests below) enough candidates left to still fill a full rail.
            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                ),
            )
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            useCase().first()

            // ----- Assert -----
            coVerify {
                exploreRepository.fetchBooksByGenre(
                    genre = "Romance",
                    limit = match { it > DISPLAY_LIMIT },
                )
            }
        }

        @Test
        fun `derives the genre from the highest-count genre slice`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 3,
                    startId = 0,
                ) +
                    booksTaggedWithGenre(
                        genre = "Horror",
                        count = 10,
                        startId = 100,
                    ),
            )
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            useCase().first()

            // ----- Assert -----
            coVerify {
                exploreRepository.fetchBooksByGenre(
                    genre = "Horror",
                    limit = any(),
                )
            }
        }

        @Test
        fun `excludes a fetched book already on one of the user's shelves`() = runTest {
            // ----- Arrange -----
            val shelvedBook = stubBook(
                id = 10,
                genreTags = listOf("Romance"),
            )
            val unshelvedBook = stubBook(id = 11)

            every {
                booksRepository.books
            } returns flowOf(listOf(shelvedBook))
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } returns listOf(shelvedBook, unshelvedBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.books shouldBe listOf(unshelvedBook)
        }

        @Test
        fun `excludes a fetched book already on one of the user's lists`() = runTest {
            // ----- Arrange -----
            val listedBook = stubBook(id = 20)
            val unlistedBook = stubBook(id = 21)
            val listWithBook = BookList(
                id = 1,
                name = "To read",
                slug = "to-read",
                books = listOf(
                    ListBook(
                        listBookId = 0,
                        listId = 1,
                        bookId = 20,
                        editionId = 0,
                    ),
                ),
            )

            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 1,
                    startId = 100,
                ),
            )
            every {
                listsRepository.allUserLists
            } returns flowOf(listOf(listWithBook))
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } returns listOf(listedBook, unlistedBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.books shouldBe listOf(unlistedBook)
        }

        @Test
        fun `keeps a fetched book that is neither shelved nor listed`() = runTest {
            // ----- Arrange -----
            val fetchedBook = stubBook(id = 10)
            val unrelatedUserBook = stubBook(
                id = 99,
                genreTags = listOf("Romance"),
            )

            every {
                booksRepository.books
            } returns flowOf(listOf(unrelatedUserBook))
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } returns listOf(fetchedBook)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.books shouldBe listOf(fetchedBook)
        }

        @Test
        fun `prefers the persisted genre choice over the derived default`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                ),
            )
            every {
                getBecauseYouReadGenreUseCase()
            } returns flowOf("Sci-Fi")
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            useCase().first()

            // ----- Assert -----
            coVerify {
                exploreRepository.fetchBooksByGenre(
                    genre = "Sci-Fi",
                    limit = any(),
                )
            }
        }

        @Test
        fun `caps the final book list at the rail's display limit after exclusion`() = runTest {
            // ----- Arrange -----
            val fetchedBooks = (0 until 15).map { offset -> stubBook(id = 1_000 + offset) }

            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                ),
            )
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } returns fetchedBooks

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.books shouldBe fetchedBooks.take(DISPLAY_LIMIT)
        }

        @Test
        fun `drops a book from the result reactively once it is shelved after the fetch settles`() = runTest {
            // ----- Arrange -----
            val fetchedBook = stubBook(id = 50)
            val booksFlow = MutableStateFlow(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                    startId = 0,
                ),
            )

            every {
                booksRepository.books
            } returns booksFlow
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Romance",
                    limit = any(),
                )
            } returns listOf(fetchedBook)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem()?.books shouldBe listOf(fetchedBook)

                booksFlow.value = booksFlow.value + fetchedBook

                var recommendation = awaitItem()
                while (recommendation?.books?.contains(fetchedBook) == true) {
                    recommendation = awaitItem()
                }
                recommendation?.books shouldBe emptyList()

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `drops a book from the result reactively once it is added to a list after the fetch settles`() = runTest {
            // ----- Arrange -----
            val listedBook = stubBook(id = 60)
            val listsFlow = MutableStateFlow<List<BookList>>(emptyList())

            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                ),
            )
            every {
                listsRepository.allUserLists
            } returns listsFlow
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Romance",
                    limit = any(),
                )
            } returns listOf(listedBook)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem()?.books shouldBe listOf(listedBook)

                listsFlow.value = listOf(
                    BookList(
                        id = 1,
                        name = "To read",
                        slug = "to-read",
                        books = listOf(
                            ListBook(
                                listBookId = 0,
                                listId = 1,
                                bookId = 60,
                                editionId = 0,
                            ),
                        ),
                    ),
                )

                var recommendation = awaitItem()
                while (recommendation?.books?.contains(listedBook) == true) {
                    recommendation = awaitItem()
                }
                recommendation?.books shouldBe emptyList()

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `re-derives the recommendation reactively when the top genre changes`() = runTest {
            // ----- Arrange -----
            val romanceBook = stubBook(id = 1)
            val horrorBook = stubBook(id = 2)
            val booksFlow = MutableStateFlow(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                    startId = 10,
                ),
            )

            every {
                booksRepository.books
            } returns booksFlow
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Romance",
                    limit = any(),
                )
            } returns listOf(romanceBook)
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Horror",
                    limit = any(),
                )
            } returns listOf(horrorBook)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem()?.genre shouldBe "Romance"

                booksFlow.value = booksTaggedWithGenre(
                    genre = "Horror",
                    count = 9,
                    startId = 100,
                )

                // The book list now drives both the genre derivation and the exclusion filter, so
                // a single library update can settle over more than one emission before both sides
                // observe it - keep reading until the genre itself has settled.
                var recommendation = awaitItem()

                while (recommendation?.genre != "Horror") {
                    recommendation = awaitItem()
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `switching from one genre to another fetches the newly selected genre fresh`() = runTest {
            // ----- Arrange -----
            val romanceBook = stubBook(id = 1)
            val horrorBook = stubBook(id = 2)
            val booksFlow = MutableStateFlow(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                    startId = 10,
                ),
            )

            every {
                booksRepository.books
            } returns booksFlow
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Romance",
                    limit = any(),
                )
            } returns listOf(romanceBook)
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Horror",
                    limit = any(),
                )
            } returns listOf(horrorBook)

            // ----- Act & Assert -----
            useCase().test {
                var recommendation = awaitItem()
                while (recommendation == null || recommendation.genre != "Romance" || recommendation.loading) {
                    recommendation = awaitItem()
                }

                booksFlow.value = booksTaggedWithGenre(
                    genre = "Horror",
                    count = 9,
                    startId = 100,
                )

                recommendation = awaitItem()
                while (recommendation == null || recommendation.genre != "Horror" || recommendation.loading) {
                    recommendation = awaitItem()
                }

                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 1) {
                exploreRepository.fetchBooksByGenre(
                    genre = "Romance",
                    limit = any(),
                )
            }
            coVerify(exactly = 1) {
                exploreRepository.fetchBooksByGenre(
                    genre = "Horror",
                    limit = any(),
                )
            }
        }

        @Test
        fun `a failed fetch for a genre is not cached and retries on the next selection of that genre`() = runTest {
            // ----- Arrange -----
            val romanceBook = stubBook(id = 1)
            val horrorBook = stubBook(id = 2)
            val booksFlow = MutableStateFlow(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                    startId = 10,
                ),
            )

            every {
                booksRepository.books
            } returns booksFlow
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Romance",
                    limit = any(),
                )
            } throws RuntimeException("network error")
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Horror",
                    limit = any(),
                )
            } returns listOf(horrorBook)

            // ----- Act & Assert -----
            useCase().test {
                var recommendation = awaitItem()
                while (recommendation == null || recommendation.genre != "Romance" || recommendation.loading) {
                    recommendation = awaitItem()
                }
                recommendation.books shouldBe emptyList()

                booksFlow.value = booksTaggedWithGenre(
                    genre = "Horror",
                    count = 9,
                    startId = 100,
                )

                recommendation = awaitItem()
                while (recommendation == null || recommendation.genre != "Horror" || recommendation.loading) {
                    recommendation = awaitItem()
                }

                coEvery {
                    exploreRepository.fetchBooksByGenre(
                        genre = "Romance",
                        limit = any(),
                    )
                } returns listOf(romanceBook)

                booksFlow.value = booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                    startId = 10,
                )

                recommendation = awaitItem()
                while (recommendation == null || recommendation.genre != "Romance" || recommendation.loading) {
                    recommendation = awaitItem()
                }
                recommendation.books shouldBe listOf(romanceBook)

                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 2) {
                exploreRepository.fetchBooksByGenre(
                    genre = "Romance",
                    limit = any(),
                )
            }
        }
    }

    // ----- Genre options -----

    @Nested
    inner class GenreOptions {
        @Test
        fun `returns all genres in frequency order when fewer than five exist`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 3,
                    startId = 0,
                ) +
                    booksTaggedWithGenre(
                        genre = "Horror",
                        count = 2,
                        startId = 10,
                    ) +
                    booksTaggedWithGenre(
                        genre = "Fantasy",
                        count = 1,
                        startId = 20,
                    ),
            )
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.genreOptions shouldBe listOf("Romance", "Horror", "Fantasy")
        }

        @Test
        fun `returns only the top five genres by frequency when more than five exist`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "A",
                    count = 7,
                    startId = 0,
                ) +
                    booksTaggedWithGenre(
                        genre = "B",
                        count = 6,
                        startId = 10,
                    ) +
                    booksTaggedWithGenre(
                        genre = "C",
                        count = 5,
                        startId = 20,
                    ) +
                    booksTaggedWithGenre(
                        genre = "D",
                        count = 4,
                        startId = 30,
                    ) +
                    booksTaggedWithGenre(
                        genre = "E",
                        count = 3,
                        startId = 40,
                    ) +
                    booksTaggedWithGenre(
                        genre = "F",
                        count = 2,
                        startId = 50,
                    ),
            )
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.genreOptions shouldBe listOf("A", "B", "C", "D", "E")
        }

        @Test
        fun `breaks ties between equally-frequent genres alphabetically`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "Zeta",
                    count = 2,
                    startId = 0,
                ) +
                    booksTaggedWithGenre(
                        genre = "Alpha",
                        count = 2,
                        startId = 10,
                    ),
            )
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = any(),
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.genreOptions shouldBe listOf("Alpha", "Zeta")
        }
    }

    // ----- Loading placeholder (genre-switch clobber guard) -----

    @Nested
    inner class LoadingPlaceholder {
        @Test
        fun `does not emit a loading placeholder for the very first genre resolution`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                ),
            )
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Romance",
                    limit = any(),
                )
            } returns listOf(stubBook(id = 1))

            // ----- Act & Assert -----
            useCase().test {
                val first = awaitItem()

                first?.genre shouldBe "Romance"
                first?.loading shouldBe false

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits a loading placeholder for the new genre before the fetched result on a genre switch`() = runTest {
            // ----- Arrange -----
            val horrorBook = stubBook(id = 2)
            val booksFlow = MutableStateFlow(
                booksTaggedWithGenre(
                    genre = "Romance",
                    count = 5,
                    startId = 10,
                ),
            )

            every {
                booksRepository.books
            } returns booksFlow
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Romance",
                    limit = any(),
                )
            } returns emptyList()
            coEvery {
                exploreRepository.fetchBooksByGenre(
                    genre = "Horror",
                    limit = any(),
                )
            } returns listOf(horrorBook)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem()?.genre shouldBe "Romance"

                booksFlow.value = booksTaggedWithGenre(
                    genre = "Horror",
                    count = 9,
                    startId = 100,
                )

                // Same clobber window as the reactive-genre test above - keep reading until the
                // NEW genre's loading placeholder (not a stale re-emission of the old genre) shows up.
                var placeholder = awaitItem()
                while (placeholder?.genre != "Horror" || placeholder.loading != true) {
                    placeholder = awaitItem()
                }
                placeholder.books shouldBe emptyList()

                var settled = awaitItem()
                while (settled?.genre != "Horror" || settled.loading != false) {
                    settled = awaitItem()
                }
                settled.books shouldBe listOf(horrorBook)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
