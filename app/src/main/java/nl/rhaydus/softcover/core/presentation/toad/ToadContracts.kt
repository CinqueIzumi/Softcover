package nl.rhaydus.softcover.core.presentation.toad

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface UiState

interface UiEvent

interface LocalVariables

abstract class ActionDependencies {
    abstract val coroutineScope: CoroutineScope
    abstract val mainDispatcher: CoroutineDispatcher

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch(
            context = mainDispatcher,
            block = block,
        )
    }
}

interface UiAction<D : ActionDependencies, S : UiState, E : UiEvent, V : LocalVariables> {
    suspend fun execute(
        dependencies: D,
        scope: ActionScope<S, E, V>,
    )
}

interface FlowCollector<S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> {
    suspend fun onLaunch(
        scope: ActionScope<S, E, V>,
        dependencies: D,
    )
}

class ActionScope<S : UiState, E : UiEvent, V : LocalVariables>(
    private val stateFlow: MutableStateFlow<S>,
    private val localVariablesFlow: MutableStateFlow<V>,
    private val eventChannel: Channel<E>,
) {
    val currentState: S
        get() = stateFlow.value

    val currentLocalVariables: V
        get() = localVariablesFlow.value

    fun setState(reducer: (S) -> S) = stateFlow.update(reducer)

    fun setLocalVariables(reducer: (V) -> V) = localVariablesFlow.update(reducer)

    fun sendEvent(event: E) = eventChannel.trySend(event)
}
