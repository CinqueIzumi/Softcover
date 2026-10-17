package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagsLoad
import nl.rhaydus.softcover.feature.book_detail.presentation.state.withTagDraft
import nl.rhaydus.toad.ActionScope

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

        val current = (scope.currentState.userTags as? UserTagsLoad.Loaded)?.tags ?: return

        val alreadyPresent = current.any {
            it.category == category && it.name.equals(
                trimmed,
                ignoreCase = true,
            )
        }

        if (alreadyPresent) return

        val newSet = current + UserTag(
            name = trimmed,
            category = category,
        )

        scope.setState {
            it.withTagDraft(input = "").copy(
                newlyAddedTagKey = "${category.name}:$trimmed",
            )
        }

        scope.commitUserTags(
            newSet = newSet,
            dependencies = dependencies,
        )
    }
}
