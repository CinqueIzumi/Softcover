package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.lists.domain.model.toHiddenRecommendation
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnRecommendedMenuDismissedAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
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
