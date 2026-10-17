package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.feature.settings.presentation.event.DocumentPageEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.DocumentPageDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState
import nl.rhaydus.toad.UiAction

internal sealed interface DocumentPageAction : UiAction<
        DocumentPageDependencies,
        DocumentPageUiState,
        DocumentPageEvent,
        DocumentPageLocalVariables,
        >
