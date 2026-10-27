package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/** Clears the selected day once the sheet has slid away, then opens the book a row asked for, if any. */
internal class OnDayDetailSheetDismissedAction : StatsAtlasAction {
    override suspend fun execute(
        dependencies: StatsAtlasDependencies,
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
    ) {
        val pending = scope.currentState.dayDetailSheet.dismissed()

        scope.setState {
            it.copy(
                dayDetailSheet = SheetSession.Closed,
                selectedReadingDay = null,
            )
        }

        if (pending == null) return

        scope.sendEvent(
            event = StatsAtlasEvent.OpenBookDetail(
                bookId = pending.bookId,
                initialCover = pending.initialCover,
            ),
        )
    }
}
