package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Overrides the gallery's rendered theme mode with [mode], so a component can be previewed in a
 * brightness other than the reader's own current setting. Selecting the already-selected mode
 * clears the override back to `null` (tap-to-toggle-off), returning the gallery to the app's own
 * theme without a separate "reset" action.
 */
internal class OnGalleryThemeModeSelectedAction(val mode: ThemeMode) : ComponentGalleryAction {
    override suspend fun execute(
        dependencies: ComponentGalleryDependencies,
        scope: ActionScope<ComponentGalleryUiState, ComponentGalleryEvent, ComponentGalleryLocalVariables>,
    ) {
        scope.setState {
            it.copy(themeModeOverride = if (it.themeModeOverride == mode) null else mode)
        }
    }
}
