package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/** Nothing happens while the card is a masked stand-in. */
internal class OnMostQuotedBookTappedAction : StatsAtlasAction {
    override suspend fun execute(
        dependencies: StatsAtlasDependencies,
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
    ) {
        val inYourWords = scope.currentState.inYourWords

        val mostQuoted = inYourWords.mostQuoted ?: return

        if (inYourWords.isLoading) return

        scope.sendEvent(
            event = StatsAtlasEvent.OpenBookDetail(
                bookId = mostQuoted.bookId,
                initialCover = null,
            ),
        )
    }
}
