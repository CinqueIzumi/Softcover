package nl.rhaydus.softcover.core.designsystem.presentation.state

/**
 * Drives the re-authentication dialog shown when the stored token is rejected mid-session. [visible]
 * is raised by a session-expired signal; [isSubmitting] gates the form while a key is validated;
 * [errorMessage] surfaces a rejected key inline so the user can retry without losing local data.
 */
data class ReAuthState(
    val visible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)
