package nl.rhaydus.softcover.feature.scan.presentation.initializer

import nl.rhaydus.softcover.core.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState

sealed interface ScanInitializer : Initializer<
        ScanUiState,
        ScanEvent,
        ScanDependencies,
        LocalScanVariables,
        >
