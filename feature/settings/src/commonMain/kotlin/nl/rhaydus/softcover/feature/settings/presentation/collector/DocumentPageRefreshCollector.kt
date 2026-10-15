package nl.rhaydus.softcover.feature.settings.presentation.collector

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.settings.presentation.event.DocumentPageEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.DocumentPageDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState
import nl.rhaydus.toad.ActionScope

/**
 * Pulls a fresh copy when the page opens, subject to the repository's cache TTL, so a reader who opens a
 * page twice in an afternoon costs one request, not two. The failure is logged but not surfaced: the
 * cached or bundled copy is still on screen, and an error banner over readable content would be noise. Only
 * a failure the reader asked for, the pull-to-refresh in
 * [RefreshDocumentPageAction][nl.rhaydus.softcover.feature.settings.presentation.action.RefreshDocumentPageAction], fills the error slot.
 */
internal class DocumentPageRefreshCollector : DocumentPageCollector {
    override suspend fun onLaunch(
        scope: ActionScope<DocumentPageUiState, DocumentPageEvent, DocumentPageLocalVariables>,
        dependencies: DocumentPageDependencies,
    ) {
        dependencies.source.refresh(force = false).onFailure { error ->
            AppLog.e(
                error,
                "Failed to refresh the document page on launch",
            )
        }
    }
}
