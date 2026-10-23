package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookCandidate
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookSeed
import nl.rhaydus.softcover.core.book.domain.model.SimilarBooksPage
import nl.rhaydus.softcover.core.book.domain.repository.SimilarBooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

private const val DEFAULT_PAGE_SIZE: Int = 20

/**
 * Loads one page of similar books with a single hydration request.
 *
 * A page whose candidates are all filtered out comes back empty with `hasMore` still true; the caller asks
 * again rather than this looping, so each call stays one request against the rate limit.
 */
class LoadSimilarBooksPageUseCase(
    private val similarBooksRepository: SimilarBooksRepository,
) {
    suspend operator fun invoke(
        sourceBookId: Int,
        offset: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Result<SimilarBooksPage> = runCatchingLogged {
        val seed = similarBooksRepository.getSeed(bookId = sourceBookId)

        val shelvedIds = similarBooksRepository.getShelvedBookIds(ids = seed.rankedIds.drop(offset))

        val window = seed.takeWindow(
            offset = offset,
            pageSize = pageSize,
            shelvedIds = shelvedIds,
        )

        val candidates = if (window.ids.isEmpty()) {
            emptyList()
        } else {
            similarBooksRepository.fetchCandidates(ids = window.ids)
        }

        val shelvedCanonicalIds = similarBooksRepository.getShelvedBookIds(
            ids = candidates.mapNotNull { it.book.canonicalId },
        )

        SimilarBooksPage(
            books = seed.eligibleBooks(
                candidates = candidates,
                requestedIds = window.ids,
                shelvedIds = shelvedIds + shelvedCanonicalIds,
            ),
            nextOffset = window.nextOffset,
            hasMore = window.nextOffset < seed.rankedIds.size,
        )
    }
}

private fun SimilarBookSeed.takeWindow(
    offset: Int,
    pageSize: Int,
    shelvedIds: Set<Int>,
): SimilarIdWindow {
    val ids = mutableListOf<Int>()
    var nextOffset = offset

    while (nextOffset < rankedIds.size && ids.size < pageSize) {
        val id = rankedIds[nextOffset]

        val dropped = id == sourceBookId || id in excludedSeriesBookIds || id in shelvedIds

        if (dropped.not()) ids += id

        nextOffset++
    }

    return SimilarIdWindow(
        ids = ids,
        nextOffset = nextOffset,
    )
}

// The hydrated rows arrive unordered, so rank order is restored from the requested ids. Series ids are
// compared as a backstop for series the seed query returned only partially.
private fun SimilarBookSeed.eligibleBooks(
    candidates: List<SimilarBookCandidate>,
    requestedIds: List<Int>,
    shelvedIds: Set<Int>,
): List<Book> {
    val rankById = requestedIds.withIndex().associate { (index, id) -> id to index }

    val seen = mutableSetOf<Int>()

    return candidates
        .sortedBy { rankById[it.book.id] ?: Int.MAX_VALUE }
        .filter { candidate ->
            val key = candidate.book.canonicalId ?: candidate.book.id

            val isSource = key == sourceBookId || key == sourceCanonicalId
            val sharesSeries = candidate.seriesIds.any { it in sourceSeriesIds }

            isSource.not() && sharesSeries.not() && key !in shelvedIds && seen.add(key)
        }
        .map { it.book }
}
