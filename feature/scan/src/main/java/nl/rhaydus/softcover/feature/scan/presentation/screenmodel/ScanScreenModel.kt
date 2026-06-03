package nl.rhaydus.softcover.feature.scan.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.book.domain.usecase.AddBookByIsbnUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ResolveBookByIsbnUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ToadScreenModel
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.feature.scan.presentation.action.ScanAction
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.flows.ScanInitializer
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState

internal class ScanScreenModel(
    private val resolveBookByIsbnUseCase: ResolveBookByIsbnUseCase,
    private val addBookByIsbnUseCase: AddBookByIsbnUseCase,
    dispatchers: AppDispatchers,
) : ToadScreenModel<ScanUiState, ScanEvent, ScanDependencies, ScanInitializer, LocalScanVariables>(
    initializers = emptyList(),
    initialState = ScanUiState(),
    initialLocalVariables = LocalScanVariables(),
) {
    override val dependencies: ScanDependencies = ScanDependencies(
        resolveBookByIsbnUseCase = resolveBookByIsbnUseCase,
        addBookByIsbnUseCase = addBookByIsbnUseCase,
        coroutineScope = screenModelScope,
        mainDispatcher = dispatchers.main,
    )

    fun runAction(action: ScanAction) = dispatch(action)
}
