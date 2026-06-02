package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * Adds a tag to the user's set by name (the upsert reuses an existing tag of that name or creates a
 * new one server-side). The text input is cleared so the editor is ready for the next tag, then the
 * full set is committed.
 */
internal class OnAddUserTagAction(
    private val name: String,
    private val category: TagCategory,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val trimmed = name.trim()

        if (trimmed.isEmpty()) return

        val alreadyPresent = scope.currentState.userTags.any {
            it.category == category && it.name.equals(
                trimmed,
                ignoreCase = true,
            )
        }

        if (alreadyPresent) return

        val newSet = scope.currentState.userTags + UserTag(
            name = trimmed,
            category = category,
        )

        scope.setState { it.copy(tagEditorInput = "") }

        scope.commitUserTags(
            newSet = newSet,
            dependencies = dependencies,
        )
    }
}
