package nl.rhaydus.softcover.core.component.haptics

import nl.rhaydus.designsystem.haptics.Haptics

enum class Feedback { Routine, Milestone, Failure }

fun Haptics.play(feedback: Feedback?) {
    when (feedback) {
        Feedback.Routine -> select()
        Feedback.Milestone -> commit()
        Feedback.Failure -> reject()
        null -> Unit
    }
}
