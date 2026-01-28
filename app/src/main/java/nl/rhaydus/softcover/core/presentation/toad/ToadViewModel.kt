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

abstract class ToadViewModel<S : UiState, E : UiEvent>(
    initialState: S,
) : ScreenModel {
    protected abstract val dependencies: ActionDependencies

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _events = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = _events.receiveAsFlow()

    val scope: ActionScope<S, E>
        get() = ActionScope(
            stateFlow = _state,
            eventChannel = _events,
        )

    protected fun <D : ActionDependencies> dispatch(action: UiAction<D, S, E>) {
        screenModelScope.launch {
            action.execute(
                dependencies as D,
                scope = scope
            )
        }
    }
}