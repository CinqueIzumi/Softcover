package nl.rhaydus.softcover.showcase.appearance.action

import nl.rhaydus.softcover.core.component.sheet.open
import nl.rhaydus.softcover.showcase.appearance.mapper.appearanceSheet
import nl.rhaydus.softcover.showcase.appearance.screenmodel.AppearanceDependencies
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.ActionScope
import nl.rhaydus.toad.LocalVariables
import nl.rhaydus.toad.UiAction
import nl.rhaydus.toad.UiEvent

internal class OnAppearanceClickedAction<
    D,
    S : AppearanceUiState<S>,
    E : UiEvent,
    V : LocalVariables,
    > : UiAction<D, S, E, V> where D : ActionDependencies, D : AppearanceDependencies {
    override suspend fun execute(
        dependencies: D,
        scope: ActionScope<S, E, V>,
    ) {
        scope.setState {
            it.withAppearance(
                appearance = it.appearance.copy(sheet = it.appearance.sheet.open(model = appearanceSheet())),
            )
        }
    }
}
