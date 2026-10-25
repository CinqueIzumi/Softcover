package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagsLoad
import nl.rhaydus.toad.ActionScope

internal class UserTagsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { state -> state.book?.takeIf { it.userBook != null }?.id }
            .distinctUntilChanged()
            .collectLatest { shelvedBookId ->
                shelvedBookId ?: return@collectLatest

                scope.setState {
                    it.copy(
                        userTags = persistentListOf(),
                        userTagsLoad = UserTagsLoad.Loading,
                    )
                }

                coroutineScope {
                    launch {
                        val load = dependencies.refreshUserTagsForBookUseCase(bookId = shelvedBookId)
                            .onFailure { error -> AppLog.e("$error") }
                            .fold(
                                onSuccess = { UserTagsLoad.Loaded },
                                onFailure = { UserTagsLoad.Failed },
                            )

                        scope.setState { it.copy(userTagsLoad = load) }
                    }

                    dependencies.observeUserTagsForBookUseCase(bookId = shelvedBookId)
                        .collect { tags ->
                            scope.setState { it.copy(userTags = tags.toImmutableList()) }
                        }
                }
            }
    }
}
