package nl.rhaydus.softcover.core.presentation.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the one own-tag filter a screen outside the Library asked it to apply.
 * Held as state, not an event, so it survives until the Library screen model exists.
 */
class LibraryFilterRequests {
    private val _pending = MutableStateFlow<LibraryTagFilterRequest?>(null)

    val pending: StateFlow<LibraryTagFilterRequest?> = _pending.asStateFlow()

    fun request(request: LibraryTagFilterRequest) {
        _pending.value = request
    }

    /** Clears [request] only if it is still the pending one, so a newer request is never lost. */
    fun consume(request: LibraryTagFilterRequest) {
        _pending.update { current -> if (current == request) null else current }
    }

    fun clear() {
        _pending.value = null
    }
}
