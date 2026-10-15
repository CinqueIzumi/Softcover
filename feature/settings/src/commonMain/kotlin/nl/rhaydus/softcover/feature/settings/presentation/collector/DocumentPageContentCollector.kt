package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.uibinding.date.formatLongDate
import nl.rhaydus.softcover.feature.settings.presentation.event.DocumentPageEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.DocumentPageDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState
import nl.rhaydus.toad.ActionScope
import kotlin.time.Instant

/**
 * The page's single source of rendered content: the repository emits the cached copy (or the bundled
 * fallback when there is no cache yet) immediately, then re-emits after any successful refresh. The first
 * emission clears `isLoading`, so the stand-ins are only ever on screen for the first frame.
 *
 * The flow is guarded because the repository throws rather than degrading a failed read (the layered error
 * model): an unreadable cache row or bundled resource would otherwise take the screen model's coroutine
 * down. There is no `Result` to fold on a flow, so the failure is folded into the same error slot a
 * use-case failure would fill, and a retry re-fetches through
 * [RefreshDocumentPageAction][nl.rhaydus.softcover.feature.settings.presentation.action.RefreshDocumentPageAction].
 */
internal class DocumentPageContentCollector : DocumentPageCollector {
    override suspend fun onLaunch(
        scope: ActionScope<DocumentPageUiState, DocumentPageEvent, DocumentPageLocalVariables>,
        dependencies: DocumentPageDependencies,
    ) {
        dependencies.source.observe()
            .catch { error ->
                AppLog.e(
                    error,
                    "Failed to read the document page",
                )

                scope.setState {
                    it.copy(
                        isLoading = false,
                        error = it.loadFailureMessage,
                    )
                }
            }
            .collectLatest { snapshot ->
                val lastUpdatedText = snapshot.fetchedAtEpochMillis?.let { epochMillis ->
                    Instant.fromEpochMilliseconds(epochMillis)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                        .formatLongDate()
                }

                scope.setState {
                    it.copy(
                        sections = snapshot.sections,
                        isBundled = snapshot.isBundled,
                        isLoading = false,
                        lastUpdatedText = lastUpdatedText,
                    )
                }
            }
    }
}
