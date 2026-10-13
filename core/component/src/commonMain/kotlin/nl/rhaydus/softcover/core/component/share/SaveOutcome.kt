package nl.rhaydus.softcover.core.component.share

/**
 * Result of a [ShareCardCapture] save. [Cached] is a private-cache write whose [Cached.identifier] is a
 * shareable handle; [Saved] is a user-visible write; [Cancelled] means the user dismissed the save dialog,
 * which is a no-op, not an error; [Failure] carries a human-readable [Failure.reason].
 */
sealed interface SaveOutcome {

    data class Cached(
        val identifier: String,
    ) : SaveOutcome

    data class Saved(
        val identifier: String,
        val displayPath: String,
    ) : SaveOutcome

    data object Cancelled : SaveOutcome

    data class Failure(
        val reason: String,
    ) : SaveOutcome
}
