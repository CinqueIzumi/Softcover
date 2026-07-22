package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.withIndex
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetBecauseYouReadGenreAsFlowUseCase
import nl.rhaydus.softcover.feature.explore.domain.model.BecauseYouReadRecommendation
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

// Matches the genre picker's design - enough choice for the "Because you read {genre}" rail
// without overwhelming a menu with every genre the user has ever read.
private const val GENRE_OPTIONS_LIMIT = 5

// The rail's final, displayed size.
private const val GENRE_BOOKS_DISPLAY_LIMIT = 12

// Requested from the query - overfetches past [GENRE_BOOKS_DISPLAY_LIMIT] because the exclusion
// of the user's own shelved/listed books now happens client-side (see the class doc below), so
// some of what comes back won't make the final cut. 3x is a safety margin, not a measured figure.
private const val GENRE_BOOKS_FETCH_LIMIT = GENRE_BOOKS_DISPLAY_LIMIT * 3

/**
 * "Because you read {genre}" - approved deviation 2 of the explore-3a brief, later made
 * user-selectable (explore-3a feedback item 10). The effective genre is [getBecauseYouReadGenreUseCase]'s
 * persisted user choice when one exists, otherwise the user's single most-read genre derived from
 * [booksRepository]'s local library taggings (the same [buildGenreOptions] ranking that backs the
 * picker's option list, so the default is always consistent with the choices offered).
 * Deriving from the already-loaded local library - rather than the profile-stats observable, which
 * only carries data once its own background network refresh completes - means the rail no longer
 * waits on that unrelated, slower fetch to appear.
 *
 * Books carrying that genre are fetched ranked by genre-specific strength, overfetching
 * [GENRE_BOOKS_FETCH_LIMIT] candidates (see `GetBooksByGenreTag.graphql`), then excluding - HERE,
 * client-side, rather than server-side - any book already on one of the user's shelves
 * ([booksRepository]) or lists ([listsRepository]). That exclusion used to be a `_not`/`_or`
 * correlated anti-join over the user's entire library and every list, run on every request; it was
 * the rail's reported slowness, so it no longer runs on the server at all. Reacting to
 * [booksRepository] and [listsRepository] here - rather than only at fetch time - also means a book
 * the user shelves or adds to a list disappears from an already-fetched rail without needing a
 * re-fetch. Emits null when no genre can be derived and none was chosen (never surfaces as an
 * error - the section degrades to hidden).
 *
 * Clobber guard: the final `combine` below caches the latest value from each of its three sources
 * and re-emits whenever ANY of them ticks - including [booksRepository]'s own book flow, which this
 * use case also reacts to for the owned-book exclusion. Without help, a genre switch would leave a
 * window - the whole duration of [fetchRecommendation]'s network call - where the cached
 * recommendation is still the PREVIOUS genre's; an unrelated book/list tick in that window would
 * re-emit the stale genre and flip the rail's loading flag off before the new genre's books ever
 * arrived (the "flashes back to the old genre mid-fetch" bug). [fetchRecommendation] closes that
 * window by emitting a `loading = true` placeholder carrying the NEW genre the instant a genre
 * *switch* is detected (via [withIndex] - `index > 0` means this is not the very first resolution),
 * before doing the network fetch at all; that placeholder overwrites the stale cached value in the
 * final `combine` right away, so any later tick during the fetch re-emits the new genre's
 * placeholder instead of the old genre's settled result. The very first resolution (`index == 0`,
 * i.e. initial screen load) skips the placeholder - there is no previous settled recommendation for
 * it to clobber, and `loadingBecauseYouReadBooks` already defaults to true for that case.
 */
class GetBecauseYouReadBooksUseCase(
    private val booksRepository: BooksRepository,
    private val listsRepository: ListsRepository,
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
            .withIndex()
            .flatMapLatest { (index, genre) ->
                fetchRecommendation(
                    genre = genre,
                    isGenreSwitch = index > 0,
                )
            }

        return combine(
            recommendationFlow,
            booksRepository.books,
            listsRepository.allUserLists,
        ) { recommendation, allUserBooks, allUserLists ->
            val excludedBookIds = buildExcludedBookIds(
                allUserBooks = allUserBooks,
                allUserLists = allUserLists,
            )

            recommendation
                ?.excludingOwnedBooks(excludedBookIds = excludedBookIds)
                ?.copy(genreOptions = buildGenreOptions(books = allUserBooks))
        }
    }

    private fun fetchRecommendation(
        genre: String?,
        isGenreSwitch: Boolean,
    ): Flow<BecauseYouReadRecommendation?> {
        if (genre == null) return flowOf(null)

        return flow {
            if (isGenreSwitch) {
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

    /** Ids the user already owns - on a shelf ([allUserBooks]) or on any list ([allUserLists]). */
    private fun buildExcludedBookIds(
        allUserBooks: List<Book>,
        allUserLists: List<BookList>,
    ): Set<Int> = allUserBooks.map { it.id }.toSet() +
        allUserLists.flatMap { list -> list.books }.map { it.bookId }.toSet()

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
