package nl.rhaydus.softcover.core.presentation.util

/**
 * The handlers a tab's root layout registers to answer a tap on its already-selected nav item, keyed
 * by the feature's own `Tab` object.
 */
class TabReselectRegistry {
    private val handlers = mutableMapOf<Any, suspend () -> Boolean>()

    fun register(
        key: Any,
        handler: suspend () -> Boolean,
    ) {
        handlers[key] = handler
    }

    /** Removes [key]'s handler only if it is still [handler], so a successor's registration survives. */
    fun unregister(
        key: Any,
        handler: suspend () -> Boolean,
    ) {
        if (handlers[key] == handler) handlers.remove(key)
    }

    /** Whether [key]'s handler consumed the reselect. */
    suspend fun reselect(key: Any): Boolean = handlers[key]?.invoke() ?: false
}
