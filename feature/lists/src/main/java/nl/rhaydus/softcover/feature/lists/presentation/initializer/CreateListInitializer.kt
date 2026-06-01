package nl.rhaydus.softcover.feature.lists.presentation.initializer

import nl.rhaydus.softcover.core.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables

sealed interface CreateListInitializer : Initializer<
        CreateListUiState,
        CreateListEvent,
        CreateListDependencies,
        LocalCreateListVariables,
        >
