package nl.rhaydus.softcover.feature.session.presentation.collector

import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeDependencies
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.Collector

internal sealed interface FocusModeCollector : Collector<
        FocusModeUiState,
        FocusModeEvent,
        FocusModeDependencies,
        LocalFocusModeVariables,
        >
