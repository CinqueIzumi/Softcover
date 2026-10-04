package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.uibinding.masthead.mastheadRefreshFor
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Builds both Reading mastheads off the composition (R9/R10) — the desktop
 * [PageMastheadSize.Regular] one and the mobile [PageMastheadSize.Compact] one sharing space with
 * pull-to-refresh — and the desktop refresh action's label and enabled state, from
 * [ReadingScreenUiState.books] and [ReadingScreenUiState.isLoading].
 */
internal class MastheadCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        scope.state
            .map {
                MastheadSnapshot(
                    bookCount = it.books.size,
                    averageProgress = it.books.averageProgress(),
                    isLoading = it.isLoading,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val title = greetingForNow()
                val subtitle = buildSubtitle(
                    bookCount = snapshot.bookCount,
                    averageProgress = snapshot.averageProgress,
                )

                scope.setState {
                    it.copy(
                        masthead = PageMastheadUiModel(
                            eyebrow = mastheadEyebrowFor(isLoading = snapshot.isLoading),
                            title = title,
                            subtitle = subtitle,
                        ),
                        compactMasthead = PageMastheadUiModel(
                            title = title,
                            subtitle = subtitle,
                            size = PageMastheadSize.Compact,
                        ),
                        mastheadRefresh = mastheadRefreshFor(isLoading = snapshot.isLoading),
                    )
                }
            }
    }
}
