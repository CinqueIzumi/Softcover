package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnToggleUserTagSpoilerAction(
    private val tag: UserTag,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val newSet = scope.currentState.userTags.map {
            if (it.category == tag.category && it.name == tag.name) {
                it.copy(spoiler = it.spoiler.not())
            } else {
                it
            }
        }

        scope.commitUserTags(
            newSet = newSet,
            dependencies = dependencies,
        )
    }
}
