package nl.rhaydus.softcover.feature.lists.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables

sealed interface CreateListAction : UiAction<
        CreateListDependencies,
        CreateListUiState,
        CreateListEvent,
        LocalCreateListVariables,
        >
