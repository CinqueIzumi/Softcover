package nl.rhaydus.softcover.core.presentation.state

/**
 * Drives the re-authentication sheet shown when the stored token is rejected mid-session. [visible]
 * is raised by a session-expired signal; [isSubmitting] gates the form while a key is validated.
 */
data class ReAuthState(
    val visible: Boolean = false,
    val apiKey: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)
