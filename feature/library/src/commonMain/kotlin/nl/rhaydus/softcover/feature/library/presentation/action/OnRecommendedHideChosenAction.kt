package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.PendingRecommendedHide
import nl.rhaydus.toad.ActionScope

internal class OnRecommendedHideChosenAction(
    val choice: PendingRecommendedHide,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                recommendedMenu = it.recommendedMenu.requestClose(pending = choice),
                recommendedMenuBookId = null,
            )
        }
    }
}
