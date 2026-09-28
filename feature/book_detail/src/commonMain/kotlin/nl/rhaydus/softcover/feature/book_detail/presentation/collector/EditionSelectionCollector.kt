package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkBackdrop
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.model.EditionTrailingUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the edition selector's per-edition trailing mark and the hero's owned badge off the
 * composition (R9). Both read [BookDetailUiState.displayedEdition] and
 * [BookDetailUiState.isEditionOwned], which are themselves derived from several writers, so a
 * dedicated collector keeps the two from going stale behind a forgotten `copy`.
 */
internal class EditionSelectionCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                EditionSelectionSnapshot(
                    editions = it.editions,
                    selectedEditionId = it.selectedEditionId,
                    trackedEditionId = it.displayedEdition?.id,
                    isDisplayedEditionOwned = it.isEditionOwned(it.displayedEdition),
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val trailingMarks: ImmutableMap<Int, EditionTrailingUiModel> = snapshot.editions
                    .mapNotNull { edition -> edition.trailingEntry(snapshot) }
                    .toMap()
                    .toImmutableMap()

                scope.setState {
                    it.copy(
                        editionTrailingMarks = trailingMarks,
                        ownedCoverBadge = OWNED_BADGE.takeIf { snapshot.isDisplayedEditionOwned },
                    )
                }
            }
    }

    private fun BookEdition.trailingEntry(
        snapshot: EditionSelectionSnapshot,
    ): Pair<Int, EditionTrailingUiModel>? = when (id) {
        snapshot.selectedEditionId -> id to EditionTrailingUiModel.Selected(mark = SELECTED_MARK)
        snapshot.trackedEditionId -> id to EditionTrailingUiModel.Tracking(badge = TRACKING_BADGE)
        else -> null
    }

    private data class EditionSelectionSnapshot(
        val editions: List<BookEdition>,
        val selectedEditionId: Int?,
        val trackedEditionId: Int?,
        val isDisplayedEditionOwned: Boolean,
    )

    private companion object {
        val SELECTED_MARK = SelectionMarkUiModel(
            selected = true,
            backdrop = SelectionMarkBackdrop.Surface,
        )

        val TRACKING_BADGE = BadgeUiModel(
            label = "TRACKING NOW",
            tone = BadgeTone.Neutral,
            size = BadgeSize.Regular,
        )

        val OWNED_BADGE = BadgeUiModel(
            label = "Owned",
            tone = BadgeTone.Scrim,
            size = BadgeSize.Regular,
            icon = SoftcoverIcon.Check,
        )
    }
}
