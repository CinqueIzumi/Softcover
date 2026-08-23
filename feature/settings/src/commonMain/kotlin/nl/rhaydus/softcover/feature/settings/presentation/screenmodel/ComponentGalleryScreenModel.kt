package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.feature.settings.presentation.action.ComponentGalleryAction
import nl.rhaydus.softcover.feature.settings.presentation.collector.ComponentGalleryCollector
import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.ToadScreenModel

/**
 * Backs the Component Gallery screen (`component-contract.md` § 7.5). There are no collectors —
 * [GalleryRegistry][nl.rhaydus.softcover.core.component.gallery.GalleryRegistry] is static data, not
 * an observed source — so [initializers] is always empty; this model does not take a
 * `flows: List<ComponentGalleryCollector>` constructor parameter because it has none to receive.
 */
internal class ComponentGalleryScreenModel(
    appDispatchers: AppDispatchers,
) : ToadScreenModel<
    ComponentGalleryUiState,
    ComponentGalleryEvent,
    ComponentGalleryDependencies,
    ComponentGalleryCollector,
    ComponentGalleryLocalVariables,
    >(
    initialState = ComponentGalleryUiState(),
    initialLocalVariables = ComponentGalleryLocalVariables(),
    initializers = emptyList(),
) {
    override val dependencies = ComponentGalleryDependencies(
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    fun runAction(action: ComponentGalleryAction) = dispatch(action = action)
}
