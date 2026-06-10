package nl.rhaydus.softcover.core.designsystem.presentation.share

sealed interface ShareOutcome {

    data object Shared : ShareOutcome

    data class Failure(
        val reason: String,
    ) : ShareOutcome
}
