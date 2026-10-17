package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.cancelAndJoin
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagsLoad
import nl.rhaydus.toad.ActionScope

private typealias TagScope = ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

/**
 * Persists the user's complete tag set optimistically. The new set lands in state immediately so the
 * chips react at once; the save then re-sends the whole list (the upsert contract) and overwrites the
 * set with the server's canonical response on success, or rolls back to the prior set on failure
 * (where `safeMutation` has already surfaced the global error). A no-op unless the tags have loaded.
 */
internal suspend fun TagScope.commitUserTags(
    newSet: List<UserTag>,
    dependencies: BookDetailDependencies,
) {
    val bookId = currentState.book?.id ?: return

    val previous = currentState.userTags as? UserTagsLoad.Loaded ?: return

    setState { it.copy(userTags = UserTagsLoad.Loaded(newSet.toImmutableList())) }

    currentLocalVariables.tagSaveJob?.cancelAndJoin()

    val job = dependencies.launch {
        dependencies.saveUserTagsUseCase(
            bookId = bookId,
            tags = newSet,
        )
            .onSuccess { saved ->
                setState { it.copy(userTags = UserTagsLoad.Loaded(saved.toImmutableList())) }

                dependencies.recordAppliedTagsUseCase(saved)
            }
            .onFailure { error ->
                AppLog.e("$error")

                setState { it.copy(userTags = previous) }

                sendEvent(BookDetailEvent.MutationFailedEvent())
            }
    }

    setLocalVariables { it.copy(tagSaveJob = job) }
}
