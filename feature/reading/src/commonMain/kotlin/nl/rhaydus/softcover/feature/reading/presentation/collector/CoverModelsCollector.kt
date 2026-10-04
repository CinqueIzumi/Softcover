package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps every cover surface on the Reading screen: the featured hero and its
 * blurred backdrop, the empty-state trending tile, and the verdict sheet jacket.
 *
 * One collector rather than a patch at each write site: its inputs have several writers each, and a derived
 * field patched into every writer is one forgotten `copy` from a stale cover.
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
                    trendingBook = it.trendingBooks.firstOrNull(),
                    verdictBook = it.verdictPromptBook,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        featuredBackdropCover = snapshot.featuredBook?.toCoverUiModel(
                            variant = CoverVariant.Flat,
                        ),
                        featuredCover = snapshot.featuredBook?.let { book ->
                            book.toCoverUiModel(
                                variant = CoverVariant.Floating,
                                shareTransition = true,
                            )
                        },
                        trendingTileCover = snapshot.trendingBook?.let { book ->
                            book.toCoverUiModel(
                                variant = CoverVariant.Tile,
                                shareTransition = true,
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
                                variant = CoverVariant.Jacket,
                                fallbackCoverUrl = book.coverUrl,
                            )
                        },
                    )
                }
            }
    }
}
