package nl.rhaydus.softcover.core.component.transition

internal data class RetainedItem<T>(
    val item: T,
    val key: Any,
    val exiting: Boolean,
)

/**
 * Folds [current] into [previous], keeping every previous item that has left [current] as an exiting entry
 * at the position it last held. An item for which [animatesExit] is false is dropped at once.
 */
internal fun <T> retainExiting(
    previous: List<RetainedItem<T>>,
    current: List<T>,
    keyOf: (T) -> Any,
    animatesExit: (T) -> Boolean = { true },
): List<RetainedItem<T>> {
    val result = current
        .map {
            RetainedItem(
                item = it,
                key = keyOf(it),
                exiting = false,
            )
        }
        .toMutableList()
    val currentKeys = result.mapTo(mutableSetOf()) { it.key }

    var anchorKey: Any? = null

    previous.forEach { entry ->
        val isRemoved = entry.key !in currentKeys

        if (isRemoved && animatesExit(entry.item).not()) return@forEach

        if (isRemoved) {
            val anchorIndex = anchorKey?.let { anchor -> result.indexOfFirst { it.key == anchor } } ?: -1
            result.add(
                anchorIndex + 1,
                entry.copy(exiting = true),
            )
        }

        anchorKey = entry.key
    }

    return result
}
