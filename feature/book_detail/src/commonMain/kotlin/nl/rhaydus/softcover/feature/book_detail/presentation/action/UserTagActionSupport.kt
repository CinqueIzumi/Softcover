package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.coroutines.cancelAndJoin
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

private typealias TagScope = ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

/**
 * Persists the user's complete tag set optimistically. The new set lands in state immediately so the
 * chips react at once; the save then re-sends the whole list (the upsert contract) and overwrites the
 * set with the server's canonical response on success, or rolls back to [BookDetailUiState.userTags]'s
 * prior value on failure (where `safeMutation` has already surfaced the global error).
 */
internal suspend fun TagScope.commitUserTags(
    newSet: List<UserTag>,
    dependencies: BookDetailDependencies,
) {
    val bookId = currentState.book?.id ?: return

    val previous = currentState.userTags

    setState { it.copy(userTags = newSet) }

    currentLocalVariables.tagSaveJob?.cancelAndJoin()

    val job = dependencies.launch {
        dependencies.saveUserTagsUseCase(
            bookId = bookId,
            tags = newSet,
        )
            .onSuccess { saved -> setState { it.copy(userTags = saved) } }
            .onFailure { error ->
                AppLog.e("$error")

                setState { it.copy(userTags = previous) }
            }
    }

    setLocalVariables { it.copy(tagSaveJob = job) }
}
