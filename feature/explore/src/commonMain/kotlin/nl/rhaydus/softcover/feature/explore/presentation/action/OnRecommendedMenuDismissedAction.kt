package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.lists.domain.model.toHiddenRecommendation
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class OnRecommendedMenuDismissedAction : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        val pending = scope.currentState.recommendedMenu.dismissed()

        scope.setState {
            it.copy(
                recommendedMenu = SheetSession.Closed,
                recommendedMenuBookId = null,
            )
        }

        if (pending == null) return

        val book = scope.currentState.recommendedBooks.find { it.id == pending.bookId } ?: return

        OnHideRecommendedBookAction(book = book.toHiddenRecommendation()).execute(
            dependencies = dependencies,
            scope = scope,
        )
    }
}
