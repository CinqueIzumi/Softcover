package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Keeps [ComponentGalleryUiState.currentPalette] and [ComponentGalleryUiState.resolvedPalette] in
 * step with the app's own live theme configuration, mapped here rather than in the render
 * (`component-contract.md` R9) — [nl.rhaydus.softcover.feature.settings.presentation.screen.ComponentGalleryContent]'s
 * themed region reads [ComponentGalleryUiState.resolvedPalette] instead of resolving
 * [nl.rhaydus.softcover.core.presentation.theme.LocalThemeConfiguration]'s palette itself.
 * [ComponentGalleryUiState.paletteOverride] stays untouched here — only
 * [nl.rhaydus.softcover.feature.settings.presentation.action.OnGalleryPaletteSelectedAction] sets it.
 */
internal class GalleryThemeConfigurationCollector : ComponentGalleryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ComponentGalleryUiState, ComponentGalleryEvent, ComponentGalleryLocalVariables>,
        dependencies: ComponentGalleryDependencies,
    ) {
        dependencies.getThemeConfigurationUseCase().collectLatest { configuration ->
            val currentPalette = configuration.colorPalette.toSpinePalette()

            scope.setState {
                it.copy(
                    currentPalette = currentPalette,
                    resolvedPalette = it.paletteOverride?.toSpinePalette() ?: currentPalette,
                )
            }
        }
    }
}
