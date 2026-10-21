package nl.rhaydus.softcover.showcase.appearance.action

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.showcase.appearance.screenmodel.AppearanceDependencies
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.ActionScope
import nl.rhaydus.toad.LocalVariables
import nl.rhaydus.toad.UiAction
import nl.rhaydus.toad.UiEvent

internal class OnPaletteSelectedAction<
    D,
    S : AppearanceUiState<S>,
    E : UiEvent,
    V : LocalVariables,
    >(
    val palette: SpinePalette,
) : UiAction<D, S, E, V> where D : ActionDependencies, D : AppearanceDependencies {
    override suspend fun execute(
        dependencies: D,
        scope: ActionScope<S, E, V>,
    ) {
        dependencies.themeStore.selectPalette(palette)
    }
}
