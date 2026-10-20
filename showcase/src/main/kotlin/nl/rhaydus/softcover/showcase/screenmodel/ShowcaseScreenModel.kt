package nl.rhaydus.softcover.showcase.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.CoroutineDispatcher
import nl.rhaydus.softcover.showcase.action.ShowcaseAction
import nl.rhaydus.softcover.showcase.collector.ShowcaseCollector
import nl.rhaydus.softcover.showcase.event.ShowcaseEvent
import nl.rhaydus.softcover.showcase.state.ShowcaseLocalVariables
import nl.rhaydus.softcover.showcase.state.ShowcaseUiState
import nl.rhaydus.toad.ToadScreenModel

internal class ShowcaseScreenModel(
    mainDispatcher: CoroutineDispatcher,
) : ToadScreenModel<
    ShowcaseUiState,
    ShowcaseEvent,
    ShowcaseDependencies,
    ShowcaseCollector,
    ShowcaseLocalVariables,
    >(
    initialState = ShowcaseUiState(),
    initialLocalVariables = ShowcaseLocalVariables(),
    initializers = emptyList(),
) {
    override val dependencies = ShowcaseDependencies(
        mainDispatcher = mainDispatcher,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ShowcaseAction) = dispatch(action = action)
}
