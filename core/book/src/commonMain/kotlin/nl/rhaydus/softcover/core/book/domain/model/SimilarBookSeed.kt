package nl.rhaydus.softcover.core.book.domain.model

/**
 * What Hardcover knows about a source book's similar books, without hydrating any of them.
 *
 * [rankedIds] is Hardcover's whole ranked list, fetched once because the API cannot slice it.
 */
data class SimilarBookSeed(
    val sourceBookId: Int,
    val sourceCanonicalId: Int?,
    val rankedIds: List<Int>,
    val excludedSeriesBookIds: Set<Int>,
    val sourceSeriesIds: Set<Int>,
)
