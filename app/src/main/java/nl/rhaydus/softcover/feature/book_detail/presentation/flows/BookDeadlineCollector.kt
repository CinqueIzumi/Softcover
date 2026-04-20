package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineProgress

class BookDeadlineCollector : BookDetailInitializer {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        val bookIdFlow = scope.state
            .map { it.book?.id }
            .distinctUntilChanged()

        val bookProgressFlow = scope.state
            .map {
                Triple(
                    it.book?.id,
                    it.book?.currentEdition?.pages,
                    it.book?.userBookRead?.currentPage ?: 0,
                )
            }
            .distinctUntilChanged()

        val deadlineFlow = bookIdFlow.flatMapLatest { bookId ->
            if (bookId == null) {
                flowOf(null)
            } else {
                dependencies.observeBookDeadlineUseCase(bookId = bookId)
            }
        }

        combine(deadlineFlow, bookProgressFlow) { deadline, progress ->
            val (_, totalPages, currentPage) = progress

            val computed = if (deadline != null && totalPages != null) {
                DeadlineProgress.compute(
                    deadline = deadline,
                    currentPage = currentPage,
                    totalPages = totalPages,
                )
            } else {
                null
            }

            deadline to computed
        }.collectLatest { (deadline, computed) ->
            scope.setState { state ->
                state.copy(
                    deadline = deadline,
                    deadlineProgress = computed,
                )
            }
        }
    }
}
