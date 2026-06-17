package nl.rhaydus.softcover.core.designsystem.presentation.share

sealed interface SaveOutcome {

    data class Cached(
        val identifier: String,
    ) : SaveOutcome

    data class Saved(
        val identifier: String,
        val displayPath: String,
    ) : SaveOutcome

    data class Failure(
        val reason: String,
    ) : SaveOutcome
}
