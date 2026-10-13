package nl.rhaydus.softcover.core.component.haptics

/**
 * A `UiEvent` raised from an action's success or failure, which the screen plays through
 * `LocalHaptics.current.play(feedback)`; a click handler never plays an outcome haptic itself.
 * A null [feedback] plays nothing.
 */
interface FeedbackEvent {
    val feedback: Feedback?
}
