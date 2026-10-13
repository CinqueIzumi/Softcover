package nl.rhaydus.softcover.core.presentation.session

import nl.rhaydus.softcover.core.component.haptics.Feedback

sealed interface SessionOutcome {
    val feedback: Feedback

    data object Started : SessionOutcome {
        override val feedback = Feedback.Routine
    }

    data object StartFailed : SessionOutcome {
        override val feedback = Feedback.Failure
    }

    /** The session recap is shown, so stopping is felt as a milestone. */
    data object Stopped : SessionOutcome {
        override val feedback = Feedback.Milestone
    }

    data object StopFailed : SessionOutcome {
        override val feedback = Feedback.Failure
    }
}
