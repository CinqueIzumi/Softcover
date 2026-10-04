package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.mapper.ALSO_READING_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.reading.presentation.mapper.PICK_UP_NEXT_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.reading.presentation.mapper.PICK_UP_NEXT_TILE_COUNT
import nl.rhaydus.softcover.feature.reading.presentation.mapper.toAlsoReadingCard
import nl.rhaydus.softcover.feature.reading.presentation.mapper.toDeadlineProgress
import nl.rhaydus.softcover.feature.reading.presentation.mapper.toPickUpNextCard
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the "Also reading" rows and the empty-state "Pick up next" tiles to book cards off the
 * composition (`component-contract.md` § 7.2 R9).
 *
 * The first book is the featured card and has no row here. Derives from the books, the deadlines, the
 * cover overlays [DeadlineModelsCollector] writes and the failed-save ids, so it re-runs whenever any
 * of them changes.
 */
internal class BookCardModelsCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        scope.state
            .map {
                BookCardModelsSnapshot(
                    isLoadingFeatured = it.books.isEmpty(),
                    alsoReadingBooks = it.books.drop(1),
                    pickUpNextBooks = it.wantToReadBooks?.take(PICK_UP_NEXT_TILE_COUNT),
                    deadlines = it.deadlines,
                    deadlineCoverOverlays = it.deadlineCoverOverlays,
                    dateStyle = it.dateStyle,
                    failedMutationBookIds = it.failedMutationBookIds,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        alsoReadingCards = if (snapshot.isLoadingFeatured) {
                            ALSO_READING_STAND_IN_CARDS
                        } else {
                            snapshot.alsoReadingBooks.map { book ->
                                book.toAlsoReadingCard(
                                    deadlineProgress = book.toDeadlineProgress(snapshot.deadlines[book.id]),
                                    coverBadge = snapshot.deadlineCoverOverlays[book.id],
                                    dateStyle = snapshot.dateStyle,
                                    mutationFailed = book.id in snapshot.failedMutationBookIds,
                                )
                            }.toImmutableList()
                        },
                        pickUpNextCards = snapshot.pickUpNextBooks
                            ?.map(Book::toPickUpNextCard)
                            ?.toImmutableList()
                            ?: PICK_UP_NEXT_STAND_IN_CARDS,
                    )
                }
            }
    }
}
