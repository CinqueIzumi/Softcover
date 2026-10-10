package nl.rhaydus.softcover.core.presentation.session

sealed interface SessionOutcome {
    data object Started : SessionOutcome

    data object StartFailed : SessionOutcome

    data object Stopped : SessionOutcome

    data object StopFailed : SessionOutcome
}
