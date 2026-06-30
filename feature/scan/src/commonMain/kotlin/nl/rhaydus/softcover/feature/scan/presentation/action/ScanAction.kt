package nl.rhaydus.softcover.feature.scan.presentation.action

import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.toad.UiAction

internal sealed interface ScanAction : UiAction<
        ScanDependencies,
        ScanUiState,
        ScanEvent,
        LocalScanVariables,
        >
