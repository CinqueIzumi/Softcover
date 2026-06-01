package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

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
                val edition = it.book?.currentEdition
                ProgressSnapshot(
                    bookId = it.book?.id,
                    totalPages = edition?.pages,
                    currentPage = it.book?.userBookRead?.currentPage ?: 0,
                    totalSeconds = edition?.audioSeconds,
                    currentSeconds = it.book?.userBookRead?.currentSeconds ?: 0,
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

        combine(deadlineFlow, bookProgressFlow) { deadline, snapshot ->
            val total = when (deadline?.unit) {
                DeadlineUnit.PAGES -> snapshot.totalPages
                DeadlineUnit.SECONDS -> snapshot.totalSeconds
                null -> null
            }
            val current = when (deadline?.unit) {
                DeadlineUnit.PAGES -> snapshot.currentPage
                DeadlineUnit.SECONDS -> snapshot.currentSeconds
                null -> 0
            }

            val computed = if (deadline != null && total != null) {
                DeadlineProgress.compute(
                    deadline = deadline,
                    current = current,
                    total = total,
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
