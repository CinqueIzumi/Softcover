package nl.rhaydus.softcover.core.domain.message

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Compose-free channel for surfacing short, transient user messages (e.g. a generic network error).
 * Lives in domain so low-level layers like the network helpers can emit a message without depending
 * on the design system; the snackbar host in presentation collects [messages] and renders them.
 * Inverts what would otherwise be a network → designsystem dependency.
 */
object UserMessageNotifier {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val messages: Flow<String> = _messages.asSharedFlow()

    fun notify(message: String) {
        _messages.tryEmit(message)
    }
}
