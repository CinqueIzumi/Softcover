package nl.rhaydus.softcover.showcase.family.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.showcase.family.event.FamilyEvent
import nl.rhaydus.softcover.showcase.family.mapper.entrySections
import nl.rhaydus.softcover.showcase.family.mapper.familyTopBar
import nl.rhaydus.softcover.showcase.family.state.FamilyLocalVariables
import nl.rhaydus.softcover.showcase.family.state.FamilyUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class FamilyScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<FamilyUiState, FamilyEvent, FamilyDependencies, FamilyLocalVariables>>,
    family: GalleryFamily,
) : ToadScreenModel<
    FamilyUiState,
    FamilyEvent,
    FamilyDependencies,
    Collector<FamilyUiState, FamilyEvent, FamilyDependencies, FamilyLocalVariables>,
    FamilyLocalVariables,
    >(
    initialState = FamilyUiState(
        topBar = familyTopBar(family = family),
        entries = entrySections(family = family),
    ),
    initialLocalVariables = FamilyLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = FamilyDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(action: UiAction<FamilyDependencies, FamilyUiState, FamilyEvent, FamilyLocalVariables>) = dispatch(action = action)
}
