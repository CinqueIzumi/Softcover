package nl.rhaydus.softcover.core.designsystem.presentation.toad

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ActionScope<S : UiState, E : UiEvent, V : LocalVariables>(
    private val stateFlow: MutableStateFlow<S>,
    private val localVariablesFlow: MutableStateFlow<V>,
    private val eventChannel: Channel<E>,
) {
    val currentState: S
        get() = stateFlow.value

    val state: StateFlow<S>
        get() = stateFlow

    val currentLocalVariables: V
        get() = localVariablesFlow.value

    fun setState(reducer: (S) -> S) = stateFlow.update(reducer)

    fun setLocalVariables(reducer: (V) -> V) = localVariablesFlow.update(reducer)

    fun sendEvent(event: E) = eventChannel.trySend(event)
}
