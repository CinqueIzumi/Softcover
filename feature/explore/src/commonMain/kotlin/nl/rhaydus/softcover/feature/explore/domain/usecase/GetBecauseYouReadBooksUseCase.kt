package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetBecauseYouReadGenreAsFlowUseCase
import nl.rhaydus.softcover.feature.explore.domain.model.BecauseYouReadRecommendation
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

// Matches the genre picker's design - enough choice for the "Because you read {genre}" rail
// without overwhelming a menu with every genre the user has ever read.
private const val GENRE_OPTIONS_LIMIT = 5

private const val GENRE_BOOKS_DISPLAY_LIMIT = 12

// Overfetches past [GENRE_BOOKS_DISPLAY_LIMIT] because owned books are excluded client-side after the
// fetch. 3x is a safety margin, not a measured figure.
private const val GENRE_BOOKS_FETCH_LIMIT = GENRE_BOOKS_DISPLAY_LIMIT * 3

/**
 * "Because you read {genre}". The genre is [getBecauseYouReadGenreUseCase]'s persisted choice when one
 * exists, otherwise the user's most-read genre from [booksRepository]'s local library taggings, ranked by
 * the same [buildGenreOptions] that backs the picker, so the default is always one of the offered options.
 * Deriving it locally rather than from profile stats keeps the rail off that slower network refresh.
 *
 * Owned books, shelved or listed ([observeOwnedBookIds]), are excluded client-side: the
 * server-side anti-join over the whole library was the rail's slowness. Reacting to [observeOwnedBookIds] also
 * drops a book from an already-fetched rail as soon as the user shelves or lists it. Emits null when no
 * genre can be derived and none was chosen; the section hides rather than erroring.
 *
 * The final `combine` re-emits whenever any source ticks, owned-book flows included. On a cache miss,
 * [fetchRecommendation] first emits a `loading = true` placeholder carrying the new genre, so a tick during
 * the network fetch re-emits that placeholder instead of the previous genre's settled result, and the
 * first load shows the masked stand-in rail instead of popping in. A cached genre settles directly, with
 * no loading frame.
 */
class GetBecauseYouReadBooksUseCase(
    private val booksRepository: BooksRepository,
    private val observeOwnedBookIds: ObserveOwnedBookIdsUseCase,
    private val exploreRepository: ExploreRepository,
    private val getBecauseYouReadGenreUseCase: GetBecauseYouReadGenreAsFlowUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<BecauseYouReadRecommendation?> {
        val genreFlow = combine(
            booksRepository.books,
            getBecauseYouReadGenreUseCase(),
        ) { allUserBooks, selectedGenre ->
            selectedGenre ?: buildGenreOptions(books = allUserBooks).firstOrNull()
        }
            .distinctUntilChanged()

        val recommendationFlow = genreFlow
            .flatMapLatest { genre -> fetchRecommendation(genre = genre) }

        return combine(
            recommendationFlow,
            booksRepository.books,
            observeOwnedBookIds(),
        ) { recommendation, allUserBooks, ownedBookIds ->
            recommendation
                ?.excludingOwnedBooks(excludedBookIds = ownedBookIds)
                ?.copy(genreOptions = buildGenreOptions(books = allUserBooks))
        }
    }

    private fun fetchRecommendation(genre: String?): Flow<BecauseYouReadRecommendation?> {
        if (genre == null) return flowOf(null)

        return flow {
            if (exploreRepository.peekBooksByGenre(genre = genre) == null) {
                emit(
                    BecauseYouReadRecommendation(
                        genre = genre,
                        books = emptyList(),
                        loading = true,
                    ),
                )
            }

            val books = runCatchingLogged {
                exploreRepository.fetchBooksByGenre(
                    genre = genre,
                    limit = GENRE_BOOKS_FETCH_LIMIT,
                )
            }
                .onFailure { error ->
                    AppLog.e(
                        error,
                        "Failed to fetch because-you-read books for genre $genre",
                    )
                }
                .getOrDefault(emptyList())

            emit(
                BecauseYouReadRecommendation(
                    genre = genre,
                    books = books,
                ),
            )
        }
    }

    private fun BecauseYouReadRecommendation.excludingOwnedBooks(
        excludedBookIds: Set<Int>,
    ): BecauseYouReadRecommendation = copy(
        books = books
            .filterNot { book -> book.id in excludedBookIds }
            .take(GENRE_BOOKS_DISPLAY_LIMIT),
    )

    /** The user's top [GENRE_OPTIONS_LIMIT] genres by how often they tag [books], most-read first. */
    private fun buildGenreOptions(books: List<Book>): List<String> = books
        .flatMap { it.tags }
        .filter { it.category == TagCategory.GENRE }
        .groupingBy { it.name }
        .eachCount()
        .entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key.lowercase() },
        )
        .take(GENRE_OPTIONS_LIMIT)
        .map { it.key }
}
