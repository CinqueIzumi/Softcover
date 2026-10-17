package nl.rhaydus.softcover.feature.scan.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.AddBookByIsbnUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ResolveBookByIsbnUseCase
import nl.rhaydus.softcover.feature.scan.presentation.action.ScanAction
import nl.rhaydus.softcover.feature.scan.presentation.collector.ScanCollector
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.toad.ToadScreenModel

internal class ScanScreenModel(
    private val resolveBookByIsbnUseCase: ResolveBookByIsbnUseCase,
    private val addBookByIsbnUseCase: AddBookByIsbnUseCase,
    dispatchers: AppDispatchers,
    flows: List<ScanCollector>,
    prefilledIsbn: String? = null,
    startInManualEntry: Boolean = false,
) : ToadScreenModel<ScanUiState, ScanEvent, ScanDependencies, ScanCollector, LocalScanVariables>(
    initializers = flows,
    initialState = ScanUiState().seededWith(
        prefilledIsbn = prefilledIsbn,
        startInManualEntry = startInManualEntry,
    ),
    initialLocalVariables = LocalScanVariables(),
) {
    override val dependencies: ScanDependencies = ScanDependencies(
        resolveBookByIsbnUseCase = resolveBookByIsbnUseCase,
        addBookByIsbnUseCase = addBookByIsbnUseCase,
        coroutineScope = screenModelScope,
        mainDispatcher = dispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ScanAction) = dispatch(action)
}

private fun ScanUiState.seededWith(
    prefilledIsbn: String?,
    startInManualEntry: Boolean,
): ScanUiState = copy(
    isbnField = isbnField.copy(
        text = prefilledIsbn.orEmpty(),
        autoFocus = isbnField.autoFocus || startInManualEntry,
    ),
)
