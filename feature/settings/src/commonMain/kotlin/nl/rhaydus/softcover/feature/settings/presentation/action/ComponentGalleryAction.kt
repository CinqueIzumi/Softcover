package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.UiAction

internal sealed interface ComponentGalleryAction : UiAction<
        ComponentGalleryDependencies,
        ComponentGalleryUiState,
        ComponentGalleryEvent,
        ComponentGalleryLocalVariables,
        >
