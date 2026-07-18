package nl.rhaydus.softcover.feature.lists.presentation.action

import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables
import nl.rhaydus.toad.ActionScope

/**
 * Flips the list's privacy between the two user-selectable options. Only [PrivacySetting.PUBLIC] and
 * [PrivacySetting.PRIVATE] are reachable here — the "This shelf is public / private." sentence has
 * exactly two tappable words, so there is nothing to flip *to* for [PrivacySetting.FOLLOWERS]. Only
 * PRIVATE flips to PUBLIC; every other value — including a state that somehow arrived at FOLLOWERS —
 * flips to PRIVATE.
 */
internal class OnPrivacyToggledAction : CreateListAction {
    override suspend fun execute(
        dependencies: CreateListDependencies,
        scope: ActionScope<CreateListUiState, CreateListEvent, LocalCreateListVariables>,
    ) {
        scope.setState {
            it.copy(
                privacy = if (it.privacy == PrivacySetting.PRIVATE) {
                    PrivacySetting.PUBLIC
                } else {
                    PrivacySetting.PRIVATE
                },
            )
        }
    }
}
