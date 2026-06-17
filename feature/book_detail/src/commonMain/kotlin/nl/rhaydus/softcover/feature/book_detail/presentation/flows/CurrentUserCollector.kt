package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Keeps the current user's identity (username + avatar) in state so the personalised share card can
 * attribute the update to the reader. Sourced from the cached profile, so it resolves before the
 * share sheet is opened in practice.
 */
internal class CurrentUserCollector : BookDetailInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        dependencies.observeUserProfileDataUseCase().collectLatest { profile ->
            scope.setState {
                it.copy(
                    currentUsername = profile?.username?.takeIf { username -> username.isNotBlank() },
                    currentUserAvatarUrl = profile?.profileImageUrl?.takeIf { url -> url.isNotBlank() },
                )
            }
        }
    }
}
