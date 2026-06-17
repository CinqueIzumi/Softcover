package nl.rhaydus.softcover.feature.lists.presentation.flows

import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables
import nl.rhaydus.toad.Collector

internal sealed interface CreateListInitializer : Collector<
        CreateListUiState,
        CreateListEvent,
        CreateListDependencies,
        LocalCreateListVariables,
        >
