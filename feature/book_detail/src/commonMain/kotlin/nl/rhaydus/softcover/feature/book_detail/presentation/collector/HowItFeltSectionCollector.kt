package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.isStarted
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toHowItFeltBlock
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class HowItFeltSectionCollector : BookDetailCollector {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book?.status.isStarted() }
            .distinctUntilChanged()
            .flatMapLatest { started ->
                if (started) {
                    val bookId = scope.currentState.bookId

                    combine(
                        dependencies.observeBookMoodsUseCase(bookId = bookId),
                        dependencies.observeMoodMomentsUseCase(bookId = bookId),
                    ) { moods, moments ->
                        toHowItFeltBlock(
                            moods = moods,
                            moments = moments,
                        )
                    }
                } else {
                    flowOf(null)
                }
            }
            .collectLatest { block ->
                scope.setState { it.copy(howItFelt = block) }
            }
    }
}
