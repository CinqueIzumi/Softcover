package nl.rhaydus.softcover.showcase.home.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.showcase.home.event.HomeEvent
import nl.rhaydus.softcover.showcase.home.screenmodel.HomeDependencies
import nl.rhaydus.softcover.showcase.home.state.HomeLocalVariables
import nl.rhaydus.softcover.showcase.home.state.HomeUiState
import nl.rhaydus.toad.ActionScope

internal class OnAppearanceDismissedAction : HomeAction {
    override suspend fun execute(
        dependencies: HomeDependencies,
        scope: ActionScope<HomeUiState, HomeEvent, HomeLocalVariables>,
    ) {
        scope.setState { it.copy(appearance = it.appearance.copy(sheet = SheetSession.Closed)) }
    }
}
