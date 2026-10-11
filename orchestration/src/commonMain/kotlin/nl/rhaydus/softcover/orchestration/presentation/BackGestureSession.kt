package nl.rhaydus.softcover.orchestration.presentation

import cafe.adriel.voyager.core.screen.Screen

/**
 * One back gesture on the root stack: the screen it started on and the one it reveals. [abandoned]
 * is set when the stack changed under the gesture, after which release must not pop.
 */
internal data class BackGestureSession(
    val from: Screen,
    val target: Screen,
    val abandoned: Boolean = false,
)
