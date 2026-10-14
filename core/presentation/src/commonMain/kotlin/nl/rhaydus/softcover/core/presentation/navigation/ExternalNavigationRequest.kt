package nl.rhaydus.softcover.core.presentation.navigation

/** A place in the app that a surface outside it asks to open. */
sealed interface ExternalNavigationRequest {
    data class OpenTab(val destination: TabDestination) : ExternalNavigationRequest

    /** [bookId] is `books.id`. */
    data class OpenBook(val bookId: Int) : ExternalNavigationRequest

    data object OpenFocusMode : ExternalNavigationRequest
}
