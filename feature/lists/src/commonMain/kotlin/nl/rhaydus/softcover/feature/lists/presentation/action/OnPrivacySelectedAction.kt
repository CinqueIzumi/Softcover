package nl.rhaydus.softcover.feature.lists.presentation.action

import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables
import nl.rhaydus.softcover.feature.lists.presentation.state.privacyToggleFor
import nl.rhaydus.toad.ActionScope

/** Sets the list's privacy to the word the reader tapped, and rebuilds the sentence's model around it. */
internal class OnPrivacySelectedAction(val privacy: PrivacySetting) : CreateListAction {
    override suspend fun execute(
        dependencies: CreateListDependencies,
        scope: ActionScope<CreateListUiState, CreateListEvent, LocalCreateListVariables>,
    ) {
        scope.setState {
            it.copy(
                privacy = privacy,
                privacyToggle = privacyToggleFor(selected = privacy),
            )
        }
    }
}
