package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * One handler for every card of a surface; the cards route their events here by [BookCardEvent.key].
 *
 * [onEvent] receives the payload [resolve] returns for the event's key unchanged. An event whose key
 * [resolve] does not know is dropped. The lambdas are read through
 * `rememberUpdatedState`, so the returned handler stays the same instance across recompositions.
 */
@Composable
fun <T : Any> rememberBookCardEventHandler(
    resolve: (BookCardKey) -> T?,
    onEvent: (event: BookCardEvent, payload: T) -> Unit,
): (BookCardEvent) -> Unit {
    val currentResolve by rememberUpdatedState(resolve)
    val currentOnEvent by rememberUpdatedState(onEvent)

    return remember {
        { event ->
            currentResolve(event.key)?.let { payload ->
                currentOnEvent(
                    event,
                    payload,
                )
            }
        }
    }
}
