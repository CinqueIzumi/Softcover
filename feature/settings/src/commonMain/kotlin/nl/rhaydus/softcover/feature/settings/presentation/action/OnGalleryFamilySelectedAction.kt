package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Sets the gallery's family filter to [family]. Selecting the already-selected family clears it
 * back to `null` (tap-to-toggle-off), so the filter is dismissible from within the same control that
 * sets it, with no separate "all families" action needed.
 */
internal class OnGalleryFamilySelectedAction(val family: GalleryFamily) : ComponentGalleryAction {
    override suspend fun execute(
        dependencies: ComponentGalleryDependencies,
        scope: ActionScope<ComponentGalleryUiState, ComponentGalleryEvent, ComponentGalleryLocalVariables>,
    ) {
        scope.setState {
            it.copy(selectedFamily = if (it.selectedFamily == family) null else family)
        }
    }
}
