package nl.rhaydus.softcover.showcase.live.field.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.showcase.live.field.event.FieldLiveEvent
import nl.rhaydus.softcover.showcase.live.field.mapper.fieldLiveState
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.Collector
import nl.rhaydus.toad.ToadScreenModel
import nl.rhaydus.toad.UiAction

internal class FieldLiveScreenModel(
    appDispatchers: AppDispatchers,
    themeStore: ShowcaseThemeStore,
    initializers: List<Collector<FieldLiveUiState, FieldLiveEvent, FieldLiveDependencies, FieldLiveLocalVariables>>,
) : ToadScreenModel<
    FieldLiveUiState,
    FieldLiveEvent,
    FieldLiveDependencies,
    Collector<FieldLiveUiState, FieldLiveEvent, FieldLiveDependencies, FieldLiveLocalVariables>,
    FieldLiveLocalVariables,
    >(
    initialState = fieldLiveState(),
    initialLocalVariables = FieldLiveLocalVariables(),
    initializers = initializers,
) {
    override val dependencies = FieldLiveDependencies(
        themeStore = themeStore,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(
        action: UiAction<FieldLiveDependencies, FieldLiveUiState, FieldLiveEvent, FieldLiveLocalVariables>,
    ) = dispatch(action = action)
}
