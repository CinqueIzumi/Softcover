package nl.rhaydus.softcover.core.presentation.toad

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class ToadViewModel<S : UiState, E : UiEvent, D : ActionDependencies, F : FlowCollector<S, E, D>>(
    initialState: S,
    private val initialFlowCollectors: List<F>,
) : ScreenModel {
    protected abstract val dependencies: D

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _events = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = _events.receiveAsFlow()

    val scope: ActionScope<S, E>
        get() = ActionScope(
            stateFlow = _state,
            eventChannel = _events,
        )

    protected fun dispatch(action: UiAction<D, S, E>) {
        screenModelScope.launch {
            action.execute(
                dependencies,
                scope = scope
            )
        }
    }

    protected fun startFlowCollectors() {
        initialFlowCollectors.forEach { collector ->
            dependencies.launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
        }
    }
}