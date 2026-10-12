package nl.rhaydus.softcover.core.component.haptics

import nl.rhaydus.designsystem.haptics.Haptics

/**
 * The outcome a [FeedbackEvent] asks the screen to play as a haptic.
 *
 * [Routine] is a routine change that succeeded or a neutral selection, [Milestone] a moment bigger than a
 * save (finishing a book, creating a list, stopping a session), and [Failure] a mutation that failed or
 * rolled back. A removal carries no feedback at all, and neither does a failure the UI already shakes for,
 * since the shake plays the reject haptic itself.
 */
enum class Feedback { Routine, Milestone, Failure }

fun Haptics.play(feedback: Feedback?) {
    when (feedback) {
        Feedback.Routine -> select()
        Feedback.Milestone -> commit()
        Feedback.Failure -> reject()
        null -> Unit
    }
}
