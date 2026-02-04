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

abstract class ToadScreenModel<S : UiState, E : UiEvent, D : ActionDependencies, F : Initializer<S, E, D, V>, V : LocalVariables>(
    initialState: S,
    initialLocalVariables: V,
    private val initializers: List<F>,
) : ScreenModel {
    protected abstract val dependencies: D

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _localState = MutableStateFlow(initialLocalVariables)
    val localState: StateFlow<V> = _localState.asStateFlow()

    private val _events = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = _events.receiveAsFlow()

    val scope: ActionScope<S, E, V>
        get() = ActionScope(
            localVariablesFlow = _localState,
            stateFlow = _state,
            eventChannel = _events,
        )

    protected fun dispatch(action: UiAction<D, S, E, V>) {
        screenModelScope.launch {
            action.execute(
                dependencies,
                scope = scope
            )
        }
    }

    protected fun startInitializers() {
        initializers.forEach { collector ->
            dependencies.launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
        }
    }
}