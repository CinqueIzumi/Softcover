package nl.rhaydus.softcover.feature.scan.presentation.flows

import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.toad.Collector

internal sealed interface ScanInitializer : Collector<
        ScanUiState,
        ScanEvent,
        ScanDependencies,
        LocalScanVariables,
        >
