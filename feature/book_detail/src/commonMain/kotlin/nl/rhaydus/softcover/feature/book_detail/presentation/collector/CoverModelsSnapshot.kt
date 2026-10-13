package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * The inputs every cover on the book page resolves from. [displayedEdition] is read off the state's
 * computed getter rather than recomputed here, which is safe because a collector runs off the
 * composition.
 */
internal data class CoverModelsSnapshot(
    val book: Book?,
    val displayedEdition: BookEdition?,
    val editions: List<BookEdition>,
    val initialCover: BookInitialCover?,
    val loadingBookDetails: Boolean,
    val bookId: Int,
    val transitionSurface: String?,
)

internal fun BookDetailUiState.toCoverModelsSnapshot(): CoverModelsSnapshot = CoverModelsSnapshot(
    book = book,
    displayedEdition = displayedEdition,
    editions = editions,
    initialCover = initialCover,
    loadingBookDetails = loadingBookDetails,
    bookId = bookId,
    transitionSurface = transitionSurface,
)

internal fun CoverModelsSnapshot.toCoverModels(): CoverModels {
    val edition = displayedEdition
    val fallbackEdition = book?.defaultEdition ?: initialCover?.defaultEdition
    val fallbackCoverUrl = book?.coverUrl ?: initialCover?.fallbackCoverUrl

    // The cover only reports "loading" while nothing at all has resolved, so a book whose edition
    // is already known paints immediately instead of shimmering.
    val coverHasContent = edition != null || fallbackEdition != null || fallbackCoverUrl != null
    val coverIsLoading = loadingBookDetails && book == null && coverHasContent.not()

    // Hero and full-screen cover share one key so the cover morphs into the viewer it opens.
    val coverKey = bookTransitionKey(
        element = BookTransitionElement.Cover,
        bookId = bookId,
        surface = transitionSurface,
    )

    val hero = edition.toCoverUiModel(
        defaultEdition = fallbackEdition,
        coverlessTitle = book?.title,
        variant = CoverVariant.Rounded,
        fallbackCoverUrl = fallbackCoverUrl,
        isLoading = coverIsLoading,
        sharedTransitionKey = coverKey,
    )

    // No coverlessTitle on purpose: this is the blurred decorative layer behind the hero, not a
    // cover the user reads, so a typographic jacket here would only be blurred noise.
    val backdrop = edition.toCoverUiModel(
        defaultEdition = fallbackEdition,
        coverlessTitle = null,
        variant = CoverVariant.Flat,
        fallbackCoverUrl = fallbackCoverUrl,
        isLoading = coverIsLoading,
    )

    val fullScreen = edition.toCoverUiModel(
        defaultEdition = fallbackEdition,
        coverlessTitle = book?.title,
        variant = CoverVariant.Flat,
        fallbackCoverUrl = fallbackCoverUrl,
        isLoading = coverIsLoading,
        sharedTransitionKey = coverKey,
    )

    return CoverModels(
        hero = hero,
        backdrop = backdrop,
        fullScreen = fullScreen,
        heroTitleTransitionKey = bookTransitionKey(
            element = BookTransitionElement.Title,
            bookId = bookId,
            surface = transitionSurface,
        ),
        heroRatingTransitionKey = bookTransitionKey(
            element = BookTransitionElement.Rating,
            bookId = bookId,
            surface = transitionSurface,
        ),
    )
}

/**
 * Seeds the initial state with the covers that resolve from the navigation arguments, so the first
 * frame already draws them. [CoverModelsCollector] stays the only writer once the screen model runs.
 */
internal fun BookDetailUiState.withCoverModels(): BookDetailUiState {
    val covers = toCoverModelsSnapshot().toCoverModels()

    return copy(
        heroCover = covers.hero,
        heroBackdropCover = covers.backdrop,
        fullScreenCover = covers.fullScreen,
        heroTitleTransitionKey = covers.heroTitleTransitionKey,
        heroRatingTransitionKey = covers.heroRatingTransitionKey,
    )
}
