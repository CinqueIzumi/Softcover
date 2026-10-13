package nl.rhaydus.softcover.orchestration.presentation

import cafe.adriel.voyager.core.screen.Screen

/**
 * One back gesture on the root stack, from the screen it started on to the one it reveals. The phase
 * is the type: a gesture [Seeking] the transition either commits into [Committing] on release or
 * drops to [Abandoned] when the stack changes under it, after which release must not pop.
 */
internal sealed interface BackGestureSession {
    /** Whether the gesture has gone idle, so the next progress event starts a fresh gesture. */
    val isReleased: Boolean

    fun released(): BackGestureSession

    fun following(top: Screen): BackGestureSession

    data class Seeking(
        val from: Screen,
        val target: Screen,
        override val isReleased: Boolean = false,
    ) : BackGestureSession {
        override fun released(): BackGestureSession = copy(isReleased = true)

        override fun following(top: Screen): BackGestureSession = if (from == top) {
            this
        } else {
            Abandoned(isReleased = isReleased)
        }

        fun commit(): Committing = Committing(
            from = from,
            target = target,
        )
    }

    data class Committing(
        val from: Screen,
        val target: Screen,
    ) : BackGestureSession {
        override val isReleased: Boolean = false

        override fun released(): BackGestureSession = this

        override fun following(top: Screen): BackGestureSession = this
    }

    data class Abandoned(override val isReleased: Boolean = false) : BackGestureSession {
        override fun released(): BackGestureSession = copy(isReleased = true)

        override fun following(top: Screen): BackGestureSession = this
    }
}
