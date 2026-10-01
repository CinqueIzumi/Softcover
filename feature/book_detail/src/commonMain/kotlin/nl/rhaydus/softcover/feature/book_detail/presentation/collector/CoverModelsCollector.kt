package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps every cover the book page renders — hero, blurred backdrop, full-screen viewer, verdict
 * sheet, edition sheet and tag editor — off the composition, so no composable ever resolves an
 * edition into a cover.
 *
 * A collector rather than a write-site patch because [BookDetailUiState.book] is written by
 * `UserBooksFlowCollector` plus eight actions: a derived field copied into each of those is one
 * forgotten `copy` away from a stale cover, and nothing in this repo's gates would catch it.
 */
internal class CoverModelsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.toCoverModelsSnapshot() }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val edition = snapshot.displayedEdition
                val fallbackEdition = snapshot.book?.defaultEdition ?: snapshot.initialCover?.defaultEdition
                val fallbackCoverUrl = snapshot.book?.coverUrl ?: snapshot.initialCover?.fallbackCoverUrl
                val title = snapshot.book?.title
                val covers = snapshot.toCoverModels()

                scope.setState {
                    it.copy(
                        heroCover = covers.hero,
                        heroBackdropCover = covers.backdrop,
                        fullScreenCover = covers.fullScreen,
                        // The verdict sheet passes the displayed edition as both the edition and the
                        // default, exactly as its old call site did — it deliberately does not fall
                        // back to the book's default edition.
                        verdictCover = edition.toCoverUiModel(
                            defaultEdition = edition,
                            coverlessTitle = title,
                            variant = CoverVariant.SheetHeaderJacket,
                            fallbackCoverUrl = fallbackCoverUrl,
                        ),
                        editionSheetHeaderCover = edition.toCoverUiModel(
                            defaultEdition = fallbackEdition,
                            coverlessTitle = title,
                            variant = CoverVariant.SheetHeaderJacket,
                        ),
                        // Mapped from every known edition rather than the state's derived
                        // `filteredEditions`, which re-filters on each read: the map would otherwise
                        // have to be rebuilt on every keystroke in the edition search field.
                        editionCovers = snapshot.editions.associate { edition ->
                            edition.id to edition.toCoverUiModel(
                                defaultEdition = fallbackEdition,
                                coverlessTitle = edition.title,
                                variant = CoverVariant.EditionListRow,
                            )
                        },
                        tagEditorCover = edition.toCoverUiModel(
                            defaultEdition = fallbackEdition,
                            coverlessTitle = title,
                            variant = CoverVariant.SheetHeaderJacket,
                        ),
                    )
                }
            }
    }
}
