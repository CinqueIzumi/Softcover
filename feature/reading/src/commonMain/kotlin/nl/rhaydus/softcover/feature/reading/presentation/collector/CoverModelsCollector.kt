package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

private const val PICK_UP_NEXT_TILE_COUNT = 3

/**
 * Maps every cover surface on the Reading screen off the composition (R9): the featured hero and its
 * blurred backdrop, each "also reading" row thumb, the empty-state pick-up-next tiles, the empty-state
 * trending tile, and the verdict sheet jacket.
 *
 * One collector for all six fields rather than a patch at each write site — [ReadingScreenUiState.books]
 * alone is written by `CurrentlyReadingBooksCollector` plus several actions, [ReadingScreenUiState
 * .wantToReadBooks] by `WantToReadCollector`, [ReadingScreenUiState.trendingBooks] by
 * `TrendingBooksLoader`, and [ReadingScreenUiState.verdictPromptBook] by the reading actions. A
 * derived field patched into each of those writers instead would be one forgotten `copy` from a stale
 * cover.
 */
internal class CoverModelsCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        scope.state
            .map {
                CoverModelsSnapshot(
                    featuredBook = it.books.firstOrNull(),
                    books = it.books,
                    // Mirrors EmptyCurrentlyReadingScreen's own wantToReadBooks.take(3) / firstOrNull()
                    // — a render decision pulled forward here so it lives in one place, not two.
                    pickUpNextBooks = it.wantToReadBooks.take(PICK_UP_NEXT_TILE_COUNT),
                    trendingBook = it.trendingBooks.firstOrNull(),
                    verdictBook = it.verdictPromptBook,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        featuredBackdropCover = snapshot.featuredBook?.toCoverUiModel(
                            variant = CoverVariant.ReadingHeroBackdrop,
                        ),
                        featuredCover = snapshot.featuredBook?.let { book ->
                            book.toCoverUiModel(
                                variant = CoverVariant.ReadingFeaturedHero,
                                sharedTransitionKey = bookCoverTransitionKey(
                                    editionId = book.currentEdition?.id,
                                    bookId = book.id,
                                ),
                            )
                        },
                        bookCovers = snapshot.books.associate { book ->
                            book.id to book.toCoverUiModel(
                                variant = CoverVariant.ReadingRowThumb,
                                sharedTransitionKey = bookCoverTransitionKey(
                                    editionId = book.currentEdition?.id,
                                    bookId = book.id,
                                ),
                            )
                        },
                        pickUpNextCovers = snapshot.pickUpNextBooks.associate { book ->
                            book.id to book.toCoverUiModel(
                                variant = CoverVariant.ReadingPickUpNextTile,
                                sharedTransitionKey = bookCoverTransitionKey(
                                    editionId = book.currentEdition?.id,
                                    bookId = book.id,
                                ),
                            )
                        },
                        trendingTileCover = snapshot.trendingBook?.let { book ->
                            book.toCoverUiModel(
                                variant = CoverVariant.ReadingTrendingTile,
                                sharedTransitionKey = bookCoverTransitionKey(
                                    editionId = book.currentEdition?.id,
                                    bookId = book.id,
                                ),
                            )
                        },
                        // The verdict sheet's cover has never actually fallen back to a different
                        // edition than the finished book's currentEdition — EditionImage was called
                        // with defaultEdition = verdictBook.currentEdition here, unlike every other
                        // Reading cover site, which passes the book's real defaultEdition. Preserved
                        // as-is per the component-library port's no-pixel-change rule; not fixed here.
                        verdictCover = snapshot.verdictBook?.let { book ->
                            book.currentEdition.toCoverUiModel(
                                defaultEdition = book.currentEdition,
                                coverlessTitle = book.title,
                                variant = CoverVariant.VerdictSheetJacket,
                                fallbackCoverUrl = book.coverUrl,
                            )
                        },
                    )
                }
            }
    }
}
