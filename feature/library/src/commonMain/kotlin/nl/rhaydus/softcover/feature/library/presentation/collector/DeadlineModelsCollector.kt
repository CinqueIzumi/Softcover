package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryTone
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.uibinding.deadline.toBadgeUiModel
import nl.rhaydus.softcover.core.uibinding.deadline.toDeadlineSummaryUiModel
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Computes each book's [DeadlineProgress] off the composition (`component-contract.md` § 7.2 R9)
 * and maps it to the [BadgeUiModel] and [DeadlineSummaryUiModel] the shelf renders, keyed by
 * [Book.id] into [LibraryUiState.deadlineProgressByBook], [LibraryUiState.deadlineBadges], and
 * [LibraryUiState.deadlineSummaries]. `LibraryShelf`'s `LayoutBookEntry` used to run
 * [DeadlineProgress.compute] inline during composition (R9's original violation).
 *
 * Deriving off [ActionScope.state] rather than patched into each writer: [LibraryUiState.booksByTab]
 * is written by both `AllBooksCollector` and `BooksByStatusCollector`, [LibraryUiState.deadlines] by
 * [BookDeadlinesCollector], and [LibraryUiState.dateStyle] by [DateStyleCollector]. A derived field
 * patched into each of those writers instead would be one forgotten `copy` from a stale badge.
 *
 * The mapping work runs on [LibraryDependencies.defaultDispatcher] for the same reason
 * [CoverModelsCollector] does: scanning every book across every shelf is exactly the main-thread
 * cost a UI-model mapper should not pay inline.
 *
 * [LibraryUiState.deadlineProgressByBook] is deliberately kept as the domain [DeadlineProgress]
 * rather than folded away once [deadlineBadges]/[deadlineSummaries] exist: Library's own
 * `LibraryDeadlineCountdownBadge` and the expired-cover grayscale branch need `status` and
 * `daysRemaining` directly, and neither of those is a `:core:component` component — they are
 * feature-local render decisions, not UI models this collector should invent.
 */
internal class DeadlineModelsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                DeadlineModelsSnapshot(
                    booksByTab = state.booksByTab,
                    deadlines = state.deadlines,
                    dateStyle = state.dateStyle,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val (deadlineProgressByBook, deadlineBadges, deadlineSummaries) = withContext(dependencies.defaultDispatcher) {
                    val progressByBook = snapshot.booksByTab.values.asSequence()
                        .flatten()
                        .associateBy { it.id }
                        .mapNotNull { (id, book) -> book.toDeadlineProgress(snapshot.deadlines[id])?.let { id to it } }
                        .toMap()

                    val badges = progressByBook.mapValues { (_, progress) -> progress.toBadgeUiModel() }

                    val summaries = progressByBook.mapValues { (_, progress) ->
                        progress.toDeadlineSummaryUiModel(
                            dateStyle = snapshot.dateStyle,
                            tone = DeadlineSummaryTone.OnSurface,
                        )
                    }

                    Triple(
                        progressByBook,
                        badges,
                        summaries,
                    )
                }

                scope.setState {
                    it.copy(
                        deadlineProgressByBook = deadlineProgressByBook,
                        deadlineBadges = deadlineBadges,
                        deadlineSummaries = deadlineSummaries,
                    )
                }
            }
    }
}

private fun Book.toDeadlineProgress(deadline: BookDeadline?): DeadlineProgress? {
    val edition = currentEdition ?: return null
    val bookDeadline = deadline ?: return null

    val current = when (bookDeadline.unit) {
        DeadlineUnit.PAGES -> userBookRead?.currentPage ?: 0
        DeadlineUnit.SECONDS -> userBookRead?.currentSeconds ?: 0
    }
    val total = when (bookDeadline.unit) {
        DeadlineUnit.PAGES -> edition.pages ?: 0
        DeadlineUnit.SECONDS -> edition.audioSeconds ?: 0
    }

    return DeadlineProgress.compute(
        deadline = bookDeadline,
        current = current,
        total = total,
    )
}
