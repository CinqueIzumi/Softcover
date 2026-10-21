package nl.rhaydus.softcover.showcase.home.action

import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.showcase.home.event.HomeEvent
import nl.rhaydus.softcover.showcase.home.screenmodel.HomeDependencies
import nl.rhaydus.softcover.showcase.home.state.HomeLocalVariables
import nl.rhaydus.softcover.showcase.home.state.HomeUiState
import nl.rhaydus.toad.ActionScope

internal class OnFamilyClickedAction(val family: GalleryFamily) : HomeAction {
    override suspend fun execute(
        dependencies: HomeDependencies,
        scope: ActionScope<HomeUiState, HomeEvent, HomeLocalVariables>,
    ) {
        scope.sendEvent(HomeEvent.OpenFamily(family = family))
    }
}
