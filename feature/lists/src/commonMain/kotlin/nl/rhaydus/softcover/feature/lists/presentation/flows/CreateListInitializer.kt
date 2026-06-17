package nl.rhaydus.softcover.feature.lists.presentation.flows

import nl.rhaydus.softcover.core.designsystem.presentation.toad.Collector
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables

internal sealed interface CreateListInitializer : Collector<
        CreateListUiState,
        CreateListEvent,
        CreateListDependencies,
        LocalCreateListVariables,
        >
