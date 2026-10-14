package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.feature.settings.presentation.action.DocumentPageAction
import nl.rhaydus.softcover.feature.settings.presentation.collector.DocumentPageCollector
import nl.rhaydus.softcover.feature.settings.presentation.event.DocumentPageEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.toInitialUiState
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import nl.rhaydus.softcover.feature.settings.presentation.source.DocumentPageSource
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState
import nl.rhaydus.toad.ToadScreenModel

internal class DocumentPageScreenModel(
    page: DocumentPage,
    source: DocumentPageSource,
    appDispatchers: AppDispatchers,
    flows: List<DocumentPageCollector>,
) : ToadScreenModel<
    DocumentPageUiState,
    DocumentPageEvent,
    DocumentPageDependencies,
    DocumentPageCollector,
    DocumentPageLocalVariables,
    >(
    initialState = page.toInitialUiState(),
    initialLocalVariables = DocumentPageLocalVariables(),
    initializers = flows,
) {
    override val dependencies = DocumentPageDependencies(
        source = source,
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: DocumentPageAction) = dispatch(action = action)
}
