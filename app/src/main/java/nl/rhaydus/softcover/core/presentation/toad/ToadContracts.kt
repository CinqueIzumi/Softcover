package nl.rhaydus.softcover.core.presentation.toad

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface UiState

interface UiEvent

abstract class ActionDependencies {
    abstract val coroutineScope: CoroutineScope
    abstract val mainDispatcher: CoroutineDispatcher

    fun launch(block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(
            context = mainDispatcher,
            block = block,
        )
    }
}

interface UiAction<D : ActionDependencies, S : UiState, E : UiEvent> {
    suspend fun execute(
        dependencies: D,
        scope: ActionScope<S, E>,
    )
}

class ActionScope<S : UiState, E : UiEvent>(
    private val stateFlow: MutableStateFlow<S>,
    private val eventChannel: Channel<E>,
) {
    val currentState: S
        get() = stateFlow.value

    fun setState(reducer: S.() -> S) = stateFlow.update(reducer)

    fun sendEvent(event: E) = eventChannel.trySend(event)
}
