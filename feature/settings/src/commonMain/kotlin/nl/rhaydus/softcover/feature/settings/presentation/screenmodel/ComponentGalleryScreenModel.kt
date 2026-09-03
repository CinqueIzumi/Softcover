package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.ComponentGalleryAction
import nl.rhaydus.softcover.feature.settings.presentation.collector.ComponentGalleryCollector
import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.ToadScreenModel

/**
 * Backs the Component Gallery screen (`component-contract.md` § 7.5). [flows] carries
 * [nl.rhaydus.softcover.feature.settings.presentation.collector.GalleryThemeConfigurationCollector],
 * which keeps the gallery's mapped spine-colour state in step with the app's own live theme
 * configuration — [GalleryRegistry][nl.rhaydus.softcover.core.component.gallery.GalleryRegistry]
 * itself stays static data, observed by nothing.
 */
internal class ComponentGalleryScreenModel(
    getThemeConfigurationUseCase: GetThemeConfigurationUseCase,
    appDispatchers: AppDispatchers,
    flows: List<ComponentGalleryCollector>,
) : ToadScreenModel<
    ComponentGalleryUiState,
    ComponentGalleryEvent,
    ComponentGalleryDependencies,
    ComponentGalleryCollector,
    ComponentGalleryLocalVariables,
    >(
    initialState = ComponentGalleryUiState(),
    initialLocalVariables = ComponentGalleryLocalVariables(),
    initializers = flows,
) {
    override val dependencies = ComponentGalleryDependencies(
        getThemeConfigurationUseCase = getThemeConfigurationUseCase,
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ComponentGalleryAction) = dispatch(action = action)
}
