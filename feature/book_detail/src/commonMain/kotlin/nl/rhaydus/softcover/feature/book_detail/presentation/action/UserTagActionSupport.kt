package nl.rhaydus.softcover.feature.book_detail.presentation.action

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

internal val BookDetailUiState.editableUserTags: List<UserTag>?
    get() = userTags.takeIf { userTagsLoad == UserTagsLoad.Loaded }

internal suspend fun TagScope.commitUserTags(
    newSet: List<UserTag>,
    dependencies: BookDetailDependencies,
) {
    val bookId = currentState.book?.id ?: return

    if (currentState.editableUserTags == null) return

    currentLocalVariables.tagSaveJob?.cancelAndJoin()

    val job = dependencies.launch {
        dependencies.saveUserTagsUseCase(
            bookId = bookId,
            tags = newSet,
        )
            .onSuccess { saved -> dependencies.recordAppliedTagsUseCase(saved) }
            .onFailure { error ->
                AppLog.e("$error")

                sendEvent(BookDetailEvent.MutationFailedEvent())
            }
    }

    setLocalVariables { it.copy(tagSaveJob = job) }
}
