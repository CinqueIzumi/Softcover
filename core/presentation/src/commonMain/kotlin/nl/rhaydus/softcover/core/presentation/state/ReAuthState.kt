package nl.rhaydus.softcover.core.presentation.state

import nl.rhaydus.softcover.core.component.sheet.SheetModel

/**
 * The form of the re-authentication sheet shown when the stored token is rejected mid-session.
 * [isSubmitting] gates the form while a key is validated.
 */
data class ReAuthState(
    val apiKey: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    override val closeRequested: Boolean = false,
) : SheetModel<ReAuthState> {
    override fun requestingClose(): ReAuthState = copy(closeRequested = true)
}
