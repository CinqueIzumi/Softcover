package nl.rhaydus.softcover.feature.explore.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

data class BecauseYouReadRecommendation(
    val genre: String,
    val books: List<Book>,
    /** The distinct Genre-category tags across the user's own books, for the genre picker. */
    val genreOptions: List<String> = emptyList(),
    /**
     * True only for the transient placeholder [GetBecauseYouReadBooksUseCase] emits for [genre]
     * the instant a genre *switch* is detected, before the newly-fetched books for that genre
     * arrive (see that use case's doc for why). `BecauseYouReadCollector` maps this straight onto
     * `loadingBecauseYouReadBooks` instead of hardcoding it false on every emission, so a stray
     * re-emission of an unrelated upstream (the user's book/list flows) during the fetch can't
     * clobber the picker back to the old genre mid-switch.
     */
    val loading: Boolean = false,
)
