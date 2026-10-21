package nl.rhaydus.softcover.showcase.appearance.collector

import nl.rhaydus.softcover.showcase.appearance.mapper.brightnessChips
import nl.rhaydus.softcover.showcase.appearance.mapper.paletteChips
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.ActionScope
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.LocalVariables
import nl.rhaydus.toad.UiEvent

/**
 * Keeps the appearance sheet's chips on the look the theme store holds, whoever changed it.
 *
 * ScreenModels build it directly rather than resolving it from Koin: resolved by its erased type, one
 * screen's collector would be handed to every other screen.
 */
internal class ThemeChipsCollector<
    S : AppearanceUiState<S>,
    E : UiEvent,
    D : ActionDependencies,
    L : LocalVariables,
    >(
    private val themeStore: ShowcaseThemeStore,
) : Collector<S, E, D, L> {
    override suspend fun onLaunch(
        scope: ActionScope<S, E, L>,
        dependencies: D,
    ) {
        themeStore.theme.collect { theme ->
            scope.setState {
                it.withAppearance(
                    appearance = it.appearance.copy(
                        paletteChips = paletteChips(selected = theme.palette),
                        brightnessChips = brightnessChips(selected = theme.brightness),
                    ),
                )
            }
        }
    }
}
